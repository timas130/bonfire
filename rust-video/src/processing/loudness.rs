use crate::processing::{quoted, run_pipeline_to_eos, timeout_for_duration, StateGuard};
use c_core::prelude::anyhow;
use gstreamer as gst;
use std::path::Path;

#[derive(Debug, Clone, Copy)]
pub struct AudioGain {
    pub gain_db: f64,
    pub linear: f64,
}

// pass 1: analyze audio loudness for normalization
pub fn analyze_gain(src: &Path, duration_ms: i64) -> anyhow::Result<AudioGain> {
    let src = quoted(src)?;
    let pipeline_str = format!(
        "filesrc location={src} ! decodebin3 name=d \
         d. ! queue ! audioconvert ! audioresample ! rganalysis ! fakesink sync=false \
         d. ! queue ! fakesink sync=false"
    );

    let pipeline = gst::parse::launch(&pipeline_str)?;
    let _guard = StateGuard(&pipeline);

    let mut track_gain = None;
    let mut track_peak = None;
    run_pipeline_to_eos(&pipeline, timeout_for_duration(duration_ms), |tags| {
        if let Some(gain) = tags.get::<gst::tags::TrackGain>() {
            track_gain = Some(gain.get());
        }
        if let Some(peak) = tags.get::<gst::tags::TrackPeak>() {
            track_peak = Some(peak.get());
        }
    })?;

    let gain_db = clamp_gain(track_gain.unwrap_or(0.0), track_peak);
    Ok(AudioGain {
        gain_db,
        linear: 10f64.powf(gain_db / 20.0),
    })
}

pub fn clamp_gain(track_gain_db: f64, track_peak: Option<f64>) -> f64 {
    let mut gain = track_gain_db;
    if let Some(peak) = track_peak {
        if peak > 0.0 {
            gain = gain.min(-20.0 * peak.log10());
        }
    }
    gain.clamp(-20.0, 12.0)
}
