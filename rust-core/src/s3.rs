use crate::config::ImagesConfig;
use aws_sdk_s3::config::{Credentials, Region};

/// Build an S3 client and bucket name from the images config.
pub fn build_s3(images: &ImagesConfig) -> Option<(aws_sdk_s3::Client, String)> {
    let ImagesConfig::S3 {
        endpoint,
        bucket,
        key_id,
        key_secret,
        region,
    } = images
    else {
        return None;
    };

    let config = aws_sdk_s3::Config::builder()
        .endpoint_url(endpoint)
        .credentials_provider(Credentials::new(key_id, key_secret, None, None, "Bonfire"))
        .region(Some(Region::new(region.clone())))
        .force_path_style(true)
        .build();
    let client = aws_sdk_s3::Client::from_conf(config);

    Some((client, bucket.clone()))
}
