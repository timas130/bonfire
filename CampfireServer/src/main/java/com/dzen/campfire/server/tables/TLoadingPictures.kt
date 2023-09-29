package com.dzen.campfire.server.tables

object TLoadingPictures {
    /*
    create table campfire_db.loading_pictures (
        id serial not null,
        image_id bigint not null,
        start_time bigint not null,
        end_time bigint not null,
        title_text text not null,
        subtitle_text text not null
    );
     */

    val NAME = "loading_pictures"
    val id = "id"
    val image_id = "image_id"
    val start_time = "start_time"
    val end_time = "end_time"
    val title_text = "title_text"
    val subtitle_text = "subtitle_text"
}
