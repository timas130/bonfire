use c_core::prelude::anyhow::{self, anyhow, Context as _};
use gstreamer as gst;
use gstreamer_pbutils as gst_pbutils;
use std::path::Path;

const PROBE_TIMEOUT: u64 = 15;

#[derive(Debug, Clone, Copy)]
pub struct MediaInfo {
    pub duration_ms: i64,
    pub width: u32,
    pub height: u32,
    pub fps: f64,
    pub has_audio: bool,
}

/// Probe a media file with [`gst_pbutils::Discoverer`]
pub fn probe(path: &Path) -> anyhow::Result<MediaInfo> {
    let discoverer = gst_pbutils::Discoverer::new(gst::ClockTime::from_seconds(PROBE_TIMEOUT))
        .context("failed to create a discoverer")?;
    let info = discoverer
        .discover_uri(&file_uri(path)?)
        .context("failed to read the file")?;

    let duration_ms = info.duration().map(|d| d.mseconds() as i64).unwrap_or(0);

    let video = info
        .video_streams()
        .into_iter()
        .find(|stream| !stream.is_image())
        .ok_or_else(|| anyhow!("the file has no video track"))?;

    let framerate = video.framerate();
    let fps = if framerate.numer() > 0 && framerate.denom() > 0 {
        framerate.numer() as f64 / framerate.denom() as f64
    } else {
        30.0
    };

    Ok(MediaInfo {
        duration_ms,
        width: video.width(),
        height: video.height(),
        fps,
        has_audio: !info.audio_streams().is_empty(),
    })
}

/// Turn a local path into a `file://` URI for GStreamer
pub(crate) fn file_uri(path: &Path) -> anyhow::Result<String> {
    let absolute = std::path::absolute(path)?;
    let uri = gst::glib::filename_to_uri(absolute, None).context("failed to build a file uri")?;
    Ok(uri.to_string())
}
