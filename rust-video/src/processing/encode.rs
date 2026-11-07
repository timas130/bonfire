use crate::processing::{quoted, run_pipeline_to_eos, timeout_for_duration, MediaInfo, StateGuard};
use c_core::prelude::anyhow::{self, Context as _};
use gstreamer as gst;
use std::path::Path;

// pass 2: actual encode
pub fn encode(
    src: &Path,
    out: &Path,
    faststart_tmp: &Path,
    info: &MediaInfo,
    audio_gain_linear: Option<f64>,
) -> anyhow::Result<()> {
    let src = quoted(src)?;
    let out = quoted(out)?;
    let faststart_tmp = quoted(faststart_tmp)?;
    let key_int = (2.0 * info.fps.clamp(24.0, 30.0)).round() as i64;

    // FFMPEG IS D O N E. GSTREAMER IS BOOMING
    let video_branch = format!(
        "d. ! queue ! videoconvert ! videoflip video-direction=auto ! videoscale ! videorate ! \
         video/x-raw,format=I420,pixel-aspect-ratio=1/1,framerate=[24/1,30/1],width=[2,1080,2],height=[2,1080,2] ! \
         x264enc threads=4 speed-preset=veryfast pass=qual quantizer=23 key-int-max={key_int} ! \
         video/x-h264,profile=high ! h264parse ! queue ! mux."
    );
    let audio_branch = match audio_gain_linear {
        Some(gain) => format!(
            "d. ! queue ! audioconvert ! audioresample ! volume volume={gain:.6} ! rglimiter ! audioconvert ! \
             audio/x-raw,rate=[8000,48000],channels=[1,2] ! avenc_aac bitrate=131072 ! aacparse ! queue ! mux. "
        ),
        None => String::new(),
    };
    let pipeline_str = format!(
        "filesrc location={src} ! decodebin3 name=d \
         {video_branch} \
         {audio_branch}\
         mp4mux name=mux faststart=true faststart-file={faststart_tmp} ! filesink location={out}"
    );

    let pipeline =
        gst::parse::launch(&pipeline_str).context("failed to build the encoding pipeline")?;
    let _guard = StateGuard(&pipeline);
    run_pipeline_to_eos(&pipeline, timeout_for_duration(info.duration_ms), |_| {})?;
    Ok(())
}
