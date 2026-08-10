use crate::processing::{quoted, StateGuard};
use c_core::prelude::anyhow::{self, anyhow, bail, Context as _};
use gstreamer as gst;
use gstreamer::prelude::*;
use gstreamer_app as gst_app;
use std::path::Path;

const THUMBNAIL_TIMEOUT: u64 = 30;

pub struct Thumbnail {
    pub jpeg: Vec<u8>,
    pub width: i32,
    pub height: i32,
}

/// Grab a thumbnail from the encoded output
pub fn thumbnail(encoded: &Path, duration_ms: i64) -> anyhow::Result<Thumbnail> {
    let src = quoted(encoded)?;
    // we have to videoconvert the color twice so the colors get shown properly.
    let pipeline_str = format!(
        "filesrc location={src} ! decodebin3 ! videoconvert ! videoscale ! \
         video/x-raw,format=RGB,pixel-aspect-ratio=1/1 ! videoconvert ! \
         video/x-raw,format=I420,colorimetry=(string)1:4:7:1 ! \
         jpegenc quality=85 snapshot=true ! appsink name=sink"
    );

    let pipeline =
        gst::parse::launch(&pipeline_str).context("failed to build the thumbnail pipeline")?;
    let _guard = StateGuard(&pipeline);
    let appsink = pipeline
        .clone()
        .downcast::<gst::Pipeline>()
        .map_err(|_| anyhow!("parsed launch is not a pipeline"))?
        .by_name("sink")
        .ok_or_else(|| anyhow!("no appsink in the thumbnail pipeline"))?
        .downcast::<gst_app::AppSink>()
        .map_err(|_| anyhow!("sink is not an appsink"))?;

    pipeline
        .set_state(gst::State::Paused)
        .context("failed to pause the thumbnail pipeline")?;
    wait_preroll(&pipeline)?;

    let position_ms = 1000.min(duration_ms / 10).max(0) as u64;
    pipeline
        .seek_simple(
            gst::SeekFlags::FLUSH | gst::SeekFlags::ACCURATE,
            gst::ClockTime::from_mseconds(position_ms),
        )
        .context("failed to seek to the poster frame")?;
    wait_preroll(&pipeline)?;

    let sample = appsink
        .try_pull_preroll(gst::ClockTime::from_seconds(THUMBNAIL_TIMEOUT))
        .ok_or_else(|| anyhow!("failed to pull the poster frame"))?;
    let caps = sample
        .caps()
        .ok_or_else(|| anyhow!("no caps on the poster sample"))?;
    let structure = caps
        .structure(0)
        .ok_or_else(|| anyhow!("empty caps on the poster sample"))?;
    let width = structure.get::<i32>("width")?;
    let height = structure.get::<i32>("height")?;
    let buffer = sample
        .buffer()
        .ok_or_else(|| anyhow!("no buffer on the poster sample"))?;
    let map = buffer
        .map_readable()
        .map_err(|_| anyhow!("failed to map the poster buffer"))?;

    Ok(Thumbnail {
        jpeg: map.as_slice().to_vec(),
        width,
        height,
    })
}

fn wait_preroll(pipeline: &gst::Element) -> anyhow::Result<()> {
    let (result, _, _) = pipeline.state(gst::ClockTime::from_seconds(THUMBNAIL_TIMEOUT));
    if result.context("the thumbnail pipeline failed to preroll")? == gst::StateChangeSuccess::Async
    {
        bail!("the thumbnail pipeline hit the {THUMBNAIL_TIMEOUT}s preroll timeout");
    }
    Ok(())
}
