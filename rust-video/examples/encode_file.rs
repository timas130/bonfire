//! Local harness for the encoding pipeline
//!
//! Runs the full probe → gain → encode → thumbnail path against
//! local files, without S3 or a database:
//!
//! ```sh
//! cargo run -p b-video --example encode_file -- <input> <output.mp4>
//! ```
//!
//! The poster frame is written next to the output as `<output.mp4>.jpg`.
//! Set `GST_DEBUG=3` to diagnose pipeline issues.

use b_video::processing::{analyze_gain, encode, probe, thumbnail};
use c_core::prelude::anyhow::{self, bail, Context as _};
use std::path::PathBuf;

fn main() -> anyhow::Result<()> {
    let mut args = std::env::args().skip(1);
    let (Some(input), Some(output)) = (args.next(), args.next()) else {
        bail!("usage: encode_file <input> <output.mp4>");
    };
    let input = PathBuf::from(input);
    let output = PathBuf::from(output);

    gstreamer::init()?;

    let info = probe(&input).context("probing the input")?;
    println!("probe: {info:?}");

    let gain = if info.has_audio {
        let gain = analyze_gain(&input, info.duration_ms).context("analyzing loudness")?;
        println!("gain:  {:.2} dB (linear {:.4})", gain.gain_db, gain.linear);
        Some(gain)
    } else {
        println!("gain:  n/a (no audio track)");
        None
    };

    let faststart_tmp = output.with_extension("faststart.tmp");
    encode(
        &input,
        &output,
        &faststart_tmp,
        &info,
        gain.map(|gain| gain.linear),
    )
    .context("encoding")?;
    let encoded_size = std::fs::metadata(&output)?.len();
    println!("out:   {} ({encoded_size} bytes)", output.display());

    let final_info = probe(&output).context("probing the output")?;
    println!("final: {final_info:?}");

    let thumb = thumbnail(&output, final_info.duration_ms).context("grabbing the poster frame")?;
    let thumb_path = PathBuf::from(format!("{}.jpg", output.display()));
    std::fs::write(&thumb_path, &thumb.jpeg)?;
    println!(
        "thumb: {} ({}x{}, {} bytes)",
        thumb_path.display(),
        thumb.width,
        thumb.height,
        thumb.jpeg.len()
    );

    Ok(())
}
