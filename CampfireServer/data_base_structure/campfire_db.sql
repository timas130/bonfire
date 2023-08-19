--
-- PostgreSQL database dump
--

-- Dumped from database version 14.2 (Debian 14.2-1.pgdg110+1)
-- Dumped by pg_dump version 14.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: campfire_db; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA campfire_db;


ALTER SCHEMA campfire_db OWNER TO postgres;

--
-- Name: ensure_array(json); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.ensure_array(j json) RETURNS json
    LANGUAGE plpgsql
AS $$
begin
    select
        case when json_typeof(j) = 'string' then cast((j #>> '{}') as json)
             else j
            end
    into j;
    assert json_typeof(j) = 'array';
    return j;
end;
$$;


ALTER FUNCTION public.ensure_array(j json) OWNER TO postgres;

--
-- Name: ensure_object(json); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.ensure_object(j json) RETURNS json
    LANGUAGE plpgsql
AS $$
begin
    select
        case when json_typeof(j) = 'string' then cast((j #>> '{}') as json)
             else j
            end
    into j;
    assert json_typeof(j) = 'object';
    return j;
end;
$$;


ALTER FUNCTION public.ensure_object(j json) OWNER TO postgres;

--
-- Name: textify_unit_json(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.textify_unit_json(unit_json text) RETURNS text
    LANGUAGE plpgsql IMMUTABLE
AS $$
declare
    pages json;
    src text;
    page json;
    page_type int;
    poll_option text;
begin
    select
        json_extract_path(cast(unit_json as json), 'J_PAGES')
    into pages;
    select ensure_array(pages) into pages;

    select '' into src;
    for page in (select json_array_elements(pages) element)
        loop
            select page ->> 'J_PAGE_TYPE' into page_type;
            case
                when page_type = 1 then
                    select src || (page ->> 'J_TEXT') into src;
                when page_type = 2 then
                    select src || 'Изображение' into src;
                when page_type = 3 then
                    select src || (page ->> 'imagesCount') || ' изображений' into src;
                when page_type = 4 then
                    select src || 'Ссылка: ' || (page ->> 'name') || E'\n' || (page ->> 'link') into src;
                when page_type = 5 then
                    select src || 'Цитата: ' || (page ->> 'author') || E'\n' || (page ->> 'text') into src;
                when page_type = 6 then
                    select src || 'Спойлер на ' || (page ->> 'count') || ' страниц: ' || (page ->> 'name') into src;
                when page_type = 7 then
                    select src || 'Опрос: ' || (page ->> 'title') || E'\n' into src;
                    select src || 'Минимальный уровень: ' || (cast((page ->> 'minLevel') as int) / 100) || '\n' into src;
                    select src || 'Минимальная карма: ' || (cast((page ->> 'minKarma') as int) / 100) || '\n' into src;
                    select src || 'Минимум дней в приложении: ' || (page ->> 'minDays') || E'\n' into src;
                    -- TODO: blacklist
                    for poll_option in (select json_array_elements(ensure_array(page -> 'options')) element)
                        loop
                            select src || poll_option || E'\n' into src;
                        end loop;
                -- page_type = 8 -> unknown
                when page_type = 9 then
                    select src || 'Видео: https://youtu.be/' || (page ->> 'videoId') into src;
                -- TODO: page_type = 10 -> table
                -- TODO: page_type = 11 -> download
                -- TODO: page_type = 12 -> campfire_object
                -- TODO: page_type = 13 -> user_activity
                -- TODO: page_type = 14 -> link_image
                -- page_type = 15 -> unknown
                -- TODO: page_type = 16 -> code
                else raise warning 'unknown page type %', page_type;
                end case;
            select src || E'\n\n' into src;
        end loop;
    return src;
end;
$$;


ALTER FUNCTION public.textify_unit_json(unit_json text) OWNER TO postgres;

--
-- Name: unit_to_tsv(integer, integer, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.unit_to_tsv(language_id integer, unit_type integer, unit_json text) RETURNS tsvector
    LANGUAGE plpgsql IMMUTABLE
AS $$
declare
    lang_name text;
    src text;
begin
    select
        case when language_id = 1 then 'english'
             when language_id = 2 then 'russian'
             when language_id = 3 then 'portuguese'
             when language_id = 4 then 'ukrainian'
             when language_id = 5 then 'german'
             when language_id = 6 then 'italian'
             when language_id = 7 then 'polish'
             when language_id = 8 then 'french'
             else 'russian'
            end
    into lang_name;
    select
        case when unit_type = 1 then (cast(unit_json as json) ->> 'J_TEXT')
             when unit_type = 9 then textify_unit_json(unit_json)
             when unit_type = 8 then (cast(unit_json as json) ->> 'J_TEXT')
             else '' end
    into src;
    return to_tsvector(cast(lang_name as regconfig), src);
end;
$$;


ALTER FUNCTION public.unit_to_tsv(language_id integer, unit_type integer, unit_json text) OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: accounts; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.accounts (
                                      id bigint NOT NULL,
                                      google_id character varying(30) DEFAULT ''::character varying NOT NULL,
                                      date_create bigint DEFAULT '0'::bigint NOT NULL,
                                      account_language_id bigint DEFAULT '0'::bigint NOT NULL,
                                      name character varying(30) DEFAULT ''::character varying NOT NULL,
                                      img_id bigint DEFAULT '0'::bigint NOT NULL,
                                      ban_date bigint DEFAULT '0'::bigint NOT NULL,
                                      recruiter_id bigint DEFAULT '0'::bigint,
                                      lvl bigint DEFAULT '100'::bigint NOT NULL,
                                      karma_count bigint DEFAULT '0'::bigint NOT NULL,
                                      last_online_time bigint DEFAULT '0'::bigint NOT NULL,
                                      img_title_id bigint DEFAULT '0'::bigint NOT NULL,
                                      img_title_gif_id bigint DEFAULT '0'::bigint NOT NULL,
                                      subscribes text NOT NULL,
                                      refresh_token text NOT NULL,
                                      refresh_token_date_create bigint DEFAULT '0'::bigint NOT NULL,
                                      sex bigint DEFAULT '0'::bigint NOT NULL,
                                      reports_count bigint DEFAULT '0'::bigint NOT NULL,
                                      account_settings text NOT NULL,
                                      karma_count_total bigint DEFAULT '0'::bigint NOT NULL
);


ALTER TABLE campfire_db.accounts OWNER TO postgres;

--
-- Name: accounts_achievements; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.accounts_achievements (
                                                   id bigint NOT NULL,
                                                   account_id bigint NOT NULL,
                                                   achievement_index bigint DEFAULT '0'::bigint NOT NULL,
                                                   achievement_lvl bigint DEFAULT '0'::bigint NOT NULL,
                                                   karma_force bigint DEFAULT '0'::bigint NOT NULL
);


ALTER TABLE campfire_db.accounts_achievements OWNER TO postgres;

--
-- Name: accounts_achievements_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.accounts_achievements_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.accounts_achievements_id_seq OWNER TO postgres;

--
-- Name: accounts_achievements_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.accounts_achievements_id_seq OWNED BY campfire_db.accounts_achievements.id;


--
-- Name: accounts_effects; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.accounts_effects (
                                              id bigint NOT NULL,
                                              account_id bigint DEFAULT '0'::bigint NOT NULL,
                                              date_create bigint DEFAULT '0'::bigint NOT NULL,
                                              date_end bigint DEFAULT '0'::bigint NOT NULL,
                                              comment text NOT NULL,
                                              effect_index bigint DEFAULT '0'::bigint NOT NULL,
                                              effect_tag bigint DEFAULT '0'::bigint NOT NULL,
                                              from_account_name character varying(200) DEFAULT ''::character varying NOT NULL,
                                              effect_comment_tag bigint DEFAULT '0'::bigint NOT NULL
);


ALTER TABLE campfire_db.accounts_effects OWNER TO postgres;

--
-- Name: accounts_effects_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.accounts_effects_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.accounts_effects_id_seq OWNER TO postgres;

--
-- Name: accounts_effects_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.accounts_effects_id_seq OWNED BY campfire_db.accounts_effects.id;


--
-- Name: accounts_emails; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.accounts_emails (
                                             id bigint NOT NULL,
                                             account_id bigint DEFAULT '0'::bigint NOT NULL,
                                             date_create bigint DEFAULT '0'::bigint NOT NULL,
                                             account_email character varying(200) DEFAULT ''::character varying NOT NULL,
                                             account_password text NOT NULL
);


ALTER TABLE campfire_db.accounts_emails OWNER TO postgres;

--
-- Name: accounts_emails_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.accounts_emails_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.accounts_emails_id_seq OWNER TO postgres;

--
-- Name: accounts_emails_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.accounts_emails_id_seq OWNED BY campfire_db.accounts_emails.id;


--
-- Name: accounts_enters; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.accounts_enters (
                                             id bigint NOT NULL,
                                             account_id bigint DEFAULT '0'::bigint NOT NULL,
                                             date_create bigint DEFAULT '0'::bigint NOT NULL
);


ALTER TABLE campfire_db.accounts_enters OWNER TO postgres;

--
-- Name: accounts_enters_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.accounts_enters_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.accounts_enters_id_seq OWNER TO postgres;

--
-- Name: accounts_enters_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.accounts_enters_id_seq OWNED BY campfire_db.accounts_enters.id;


--
-- Name: accounts_firebase; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.accounts_firebase (
                                               account_id bigint NOT NULL,
                                               firebase_uid character varying(64) NOT NULL
);


ALTER TABLE campfire_db.accounts_firebase OWNER TO postgres;

--
-- Name: accounts_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.accounts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.accounts_id_seq OWNER TO postgres;

--
-- Name: accounts_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.accounts_id_seq OWNED BY campfire_db.accounts.id;


--
-- Name: accounts_notifications; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.accounts_notifications (
                                                    id bigint NOT NULL,
                                                    date_create bigint DEFAULT '0'::bigint NOT NULL,
                                                    account_id bigint DEFAULT '0'::bigint NOT NULL,
                                                    notification_json text NOT NULL,
                                                    notification_type bigint DEFAULT '0'::bigint NOT NULL,
                                                    notification_status bigint DEFAULT '0'::bigint NOT NULL
);


ALTER TABLE campfire_db.accounts_notifications OWNER TO postgres;

--
-- Name: accounts_notifications_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.accounts_notifications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.accounts_notifications_id_seq OWNER TO postgres;

--
-- Name: accounts_notifications_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.accounts_notifications_id_seq OWNED BY campfire_db.accounts_notifications.id;


--
-- Name: activities; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.activities (
                                        id bigint NOT NULL,
                                        type bigint DEFAULT '0'::bigint NOT NULL,
                                        fandom_id bigint DEFAULT '0'::bigint NOT NULL,
                                        language_id bigint DEFAULT '0'::bigint NOT NULL,
                                        date_create bigint DEFAULT '0'::bigint NOT NULL,
                                        name character varying(255) DEFAULT ''::character varying NOT NULL,
                                        image_id bigint DEFAULT '0'::bigint NOT NULL,
                                        background_id bigint DEFAULT '0'::bigint NOT NULL,
                                        creator_id bigint DEFAULT '0'::bigint NOT NULL,
                                        params text NOT NULL,
                                        tag_1 bigint DEFAULT '0'::bigint NOT NULL,
                                        tag_2 bigint DEFAULT '0'::bigint NOT NULL,
                                        tag_3 bigint DEFAULT '0'::bigint NOT NULL,
                                        tag_s_1 character varying(255) DEFAULT ''::character varying NOT NULL,
                                        tag_s_2 character varying(255) DEFAULT ''::character varying NOT NULL,
                                        tag_s_3 character varying(255) DEFAULT ''::character varying NOT NULL,
                                        description text NOT NULL
);


ALTER TABLE campfire_db.activities OWNER TO postgres;

--
-- Name: activities_collisions; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.activities_collisions (
                                                   id bigint NOT NULL,
                                                   type bigint DEFAULT '0'::bigint NOT NULL,
                                                   account_id bigint DEFAULT '0'::bigint NOT NULL,
                                                   activity_id bigint DEFAULT '0'::bigint NOT NULL,
                                                   date_create bigint DEFAULT '0'::bigint NOT NULL,
                                                   tag_1 bigint DEFAULT '0'::bigint NOT NULL,
                                                   tag_2 bigint DEFAULT '0'::bigint NOT NULL,
                                                   tag_3 bigint DEFAULT '0'::bigint NOT NULL,
                                                   tag_s_1 character varying(255) DEFAULT ''::character varying NOT NULL,
                                                   tag_s_2 character varying(255) DEFAULT ''::character varying NOT NULL,
                                                   tag_s_3 character varying(255) DEFAULT ''::character varying NOT NULL
);


ALTER TABLE campfire_db.activities_collisions OWNER TO postgres;

--
-- Name: activities_collisions_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.activities_collisions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.activities_collisions_id_seq OWNER TO postgres;

--
-- Name: activities_collisions_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.activities_collisions_id_seq OWNED BY campfire_db.activities_collisions.id;


--
-- Name: activities_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.activities_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.activities_id_seq OWNER TO postgres;

--
-- Name: activities_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.activities_id_seq OWNED BY campfire_db.activities.id;


--
-- Name: chats; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.chats (
                                   id bigint NOT NULL,
                                   type bigint DEFAULT '0'::bigint NOT NULL,
                                   fandom_id bigint DEFAULT '0'::bigint NOT NULL,
                                   language_id bigint DEFAULT '0'::bigint NOT NULL,
                                   date_create bigint DEFAULT '0'::bigint NOT NULL,
                                   name character varying(500) DEFAULT ''::character varying NOT NULL,
                                   image_id bigint DEFAULT '0'::bigint NOT NULL,
                                   creator_id bigint DEFAULT '0'::bigint NOT NULL,
                                   chat_params character varying(1000) DEFAULT ''::character varying NOT NULL,
                                   background_id bigint DEFAULT '0'::bigint NOT NULL
);


ALTER TABLE campfire_db.chats OWNER TO postgres;

--
-- Name: chats_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.chats_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.chats_id_seq OWNER TO postgres;

--
-- Name: chats_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.chats_id_seq OWNED BY campfire_db.chats.id;


--
-- Name: chats_subscriptions; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.chats_subscriptions (
                                                 id bigint NOT NULL,
                                                 account_id bigint DEFAULT '0'::bigint NOT NULL,
                                                 target_id bigint DEFAULT '0'::bigint NOT NULL,
                                                 target_sub_id bigint DEFAULT '0'::bigint,
                                                 chat_type bigint DEFAULT '0'::bigint NOT NULL,
                                                 subscribed bigint DEFAULT '0'::bigint NOT NULL,
                                                 read_date bigint DEFAULT '0'::bigint NOT NULL,
                                                 last_message_id bigint DEFAULT '0'::bigint NOT NULL,
                                                 last_message_date bigint DEFAULT '0'::bigint NOT NULL,
                                                 enter_date bigint DEFAULT '0'::bigint NOT NULL,
                                                 exit_date bigint DEFAULT '0'::bigint NOT NULL,
                                                 member_status bigint DEFAULT '0'::bigint NOT NULL,
                                                 member_level bigint DEFAULT '0'::bigint NOT NULL,
                                                 member_owner bigint DEFAULT '0'::bigint NOT NULL,
                                                 new_messages bigint DEFAULT '0'::bigint NOT NULL
);


ALTER TABLE campfire_db.chats_subscriptions OWNER TO postgres;

--
-- Name: chats_subscriptions_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.chats_subscriptions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.chats_subscriptions_id_seq OWNER TO postgres;

--
-- Name: chats_subscriptions_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.chats_subscriptions_id_seq OWNED BY campfire_db.chats_subscriptions.id;


--
-- Name: collisions; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.collisions (
                                        id bigint NOT NULL,
                                        owner_id bigint DEFAULT '0'::bigint NOT NULL,
                                        collision_type bigint DEFAULT '0'::bigint NOT NULL,
                                        collision_id bigint DEFAULT '0'::bigint NOT NULL,
                                        collision_date_create bigint DEFAULT '0'::bigint NOT NULL,
                                        collision_sub_id bigint DEFAULT '0'::bigint NOT NULL,
                                        collision_key character varying(255) DEFAULT ''::character varying NOT NULL,
                                        value_1 bigint DEFAULT '0'::bigint NOT NULL,
                                        value_2 text,
                                        value_3 bigint DEFAULT '0'::bigint NOT NULL,
                                        value_4 bigint DEFAULT '0'::bigint NOT NULL,
                                        value_5 bigint DEFAULT '0'::bigint NOT NULL
);


ALTER TABLE campfire_db.collisions OWNER TO postgres;

--
-- Name: collisions_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.collisions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.collisions_id_seq OWNER TO postgres;

--
-- Name: collisions_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.collisions_id_seq OWNED BY campfire_db.collisions.id;


--
-- Name: donate; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.donate (
                                    id bigint NOT NULL,
                                    account_id bigint DEFAULT '0'::bigint NOT NULL,
                                    sum bigint DEFAULT '0'::bigint NOT NULL,
                                    data text NOT NULL,
                                    donate_key character varying(300) DEFAULT ''::character varying NOT NULL
);


ALTER TABLE campfire_db.donate OWNER TO postgres;

--
-- Name: donate_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.donate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.donate_id_seq OWNER TO postgres;

--
-- Name: donate_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.donate_id_seq OWNED BY campfire_db.donate.id;


--
-- Name: fandoms; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.fandoms (
                                     id bigint NOT NULL,
                                     name character varying(255) DEFAULT ''::character varying NOT NULL,
                                     creator_id bigint DEFAULT '0'::bigint NOT NULL,
                                     image_id bigint DEFAULT '0'::bigint NOT NULL,
                                     image_title_id bigint DEFAULT '0'::bigint NOT NULL,
                                     date_create bigint DEFAULT '0'::bigint NOT NULL,
                                     status bigint DEFAULT '0'::bigint NOT NULL,
                                     subscribers_count bigint DEFAULT '0'::bigint NOT NULL,
                                     fandom_category bigint DEFAULT '0'::bigint NOT NULL,
                                     fandom_closed bigint DEFAULT '0'::bigint NOT NULL,
                                     karma_cof bigint DEFAULT '100'::bigint NOT NULL
);


ALTER TABLE campfire_db.fandoms OWNER TO postgres;

--
-- Name: fandoms_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.fandoms_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.fandoms_id_seq OWNER TO postgres;

--
-- Name: fandoms_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.fandoms_id_seq OWNED BY campfire_db.fandoms.id;


--
-- Name: quest_parts; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.quest_parts (
                                         id bigint NOT NULL,
                                         unit_id integer NOT NULL,
                                         json_db text NOT NULL,
                                         part_order integer DEFAULT 1 NOT NULL
);


ALTER TABLE campfire_db.quest_parts OWNER TO postgres;

--
-- Name: quest_parts_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.quest_parts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.quest_parts_id_seq OWNER TO postgres;

--
-- Name: quest_parts_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.quest_parts_id_seq OWNED BY campfire_db.quest_parts.id;


--
-- Name: rubrics; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.rubrics (
                                     id bigint NOT NULL,
                                     name character varying(500) DEFAULT ''::character varying NOT NULL,
                                     creator_id bigint DEFAULT '0'::bigint NOT NULL,
                                     karma_cof bigint DEFAULT '0'::bigint NOT NULL,
                                     fandom_id bigint DEFAULT '0'::bigint NOT NULL,
                                     language_id bigint DEFAULT '0'::bigint NOT NULL,
                                     date_create bigint DEFAULT '0'::bigint NOT NULL,
                                     owner_id bigint DEFAULT '0'::bigint NOT NULL,
                                     status bigint DEFAULT '0'::bigint NOT NULL,
                                     status_change_date bigint DEFAULT '0'::bigint NOT NULL
);


ALTER TABLE campfire_db.rubrics OWNER TO postgres;

--
-- Name: rubrics_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.rubrics_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.rubrics_id_seq OWNER TO postgres;

--
-- Name: rubrics_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.rubrics_id_seq OWNED BY campfire_db.rubrics.id;


--
-- Name: support; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.support (
                                     id bigint NOT NULL,
                                     date bigint DEFAULT '0'::bigint NOT NULL,
                                     count bigint DEFAULT '0'::bigint NOT NULL,
                                     user_id bigint DEFAULT '0'::bigint NOT NULL,
                                     date_create bigint DEFAULT '0'::bigint NOT NULL,
                                     status bigint DEFAULT '0'::bigint NOT NULL,
                                     comment character varying(500) DEFAULT ''::character varying NOT NULL,
                                     donate_info text NOT NULL
);


ALTER TABLE campfire_db.support OWNER TO postgres;

--
-- Name: support_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.support_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.support_id_seq OWNER TO postgres;

--
-- Name: support_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.support_id_seq OWNED BY campfire_db.support.id;


--
-- Name: translates; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.translates (
                                        id bigint NOT NULL,
                                        languageid bigint DEFAULT '0'::bigint NOT NULL,
                                        translate_key character varying(200) DEFAULT ''::character varying NOT NULL,
                                        text text,
                                        hint character varying(500) DEFAULT ''::character varying NOT NULL,
                                        appkey character varying(50) DEFAULT ''::character varying NOT NULL
);


ALTER TABLE campfire_db.translates OWNER TO postgres;

--
-- Name: translates_history; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.translates_history (
                                                id bigint NOT NULL,
                                                language_id bigint DEFAULT '0'::bigint NOT NULL,
                                                language_id_from bigint DEFAULT '0'::bigint NOT NULL,
                                                translate_key character varying(200) DEFAULT ''::character varying NOT NULL,
                                                old_text text,
                                                new_text text,
                                                history_type bigint DEFAULT '0'::bigint NOT NULL,
                                                history_creator_id bigint DEFAULT '0'::bigint NOT NULL,
                                                date_history_created bigint DEFAULT '0'::bigint NOT NULL,
                                                project_key character varying(200) DEFAULT ''::character varying NOT NULL,
                                                history_comment text NOT NULL,
                                                confirm_account_1 bigint DEFAULT '0'::bigint NOT NULL,
                                                confirm_account_2 bigint DEFAULT '0'::bigint NOT NULL,
                                                confirm_account_3 bigint DEFAULT '0'::bigint NOT NULL
);


ALTER TABLE campfire_db.translates_history OWNER TO postgres;

--
-- Name: translates_history_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.translates_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.translates_history_id_seq OWNER TO postgres;

--
-- Name: translates_history_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.translates_history_id_seq OWNED BY campfire_db.translates_history.id;


--
-- Name: translates_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.translates_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.translates_id_seq OWNER TO postgres;

--
-- Name: translates_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.translates_id_seq OWNED BY campfire_db.translates.id;


--
-- Name: units; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.units (
                                   id bigint NOT NULL,
                                   fandom_id bigint DEFAULT '0'::bigint NOT NULL,
                                   language_id bigint DEFAULT '0'::bigint NOT NULL,
                                   unit_type bigint DEFAULT '0'::bigint NOT NULL,
                                   unit_category bigint DEFAULT '0'::bigint NOT NULL,
                                   date_create bigint DEFAULT '0'::bigint NOT NULL,
                                   creator_id bigint DEFAULT '0'::bigint NOT NULL,
                                   unit_json text NOT NULL,
                                   parent_unit_id bigint DEFAULT '0'::bigint NOT NULL,
                                   status bigint DEFAULT '0'::bigint NOT NULL,
                                   subunits_count bigint DEFAULT '0'::bigint NOT NULL,
                                   karma_count bigint DEFAULT '0'::bigint NOT NULL,
                                   important bigint DEFAULT '0'::bigint NOT NULL,
                                   parent_fandom_closed bigint DEFAULT '0'::bigint NOT NULL,
                                   closed bigint DEFAULT '0'::bigint NOT NULL,
                                   tag_1 bigint DEFAULT '0'::bigint NOT NULL,
                                   tag_2 bigint DEFAULT '0'::bigint NOT NULL,
                                   tag_3 bigint DEFAULT '0'::bigint NOT NULL,
                                   tag_4 bigint DEFAULT '0'::bigint NOT NULL,
                                   tag_5 bigint DEFAULT '0'::bigint NOT NULL,
                                   tag_6 bigint DEFAULT '0'::bigint NOT NULL,
                                   tag_7 bigint DEFAULT '0'::bigint NOT NULL,
                                   tag_s_1 character varying(255) DEFAULT ''::character varying NOT NULL,
                                   tag_s_2 character varying(255) DEFAULT ''::character varying NOT NULL,
                                   unit_reports_count bigint DEFAULT '0'::bigint NOT NULL,
                                   fandom_key character varying(200) DEFAULT ''::character varying NOT NULL,
                                   search tsvector
);


ALTER TABLE campfire_db.units OWNER TO postgres;

--
-- Name: units_history; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.units_history (
                                           id bigint NOT NULL,
                                           unit_id bigint DEFAULT '0'::bigint NOT NULL,
                                           history_type bigint DEFAULT '0'::bigint NOT NULL,
                                           data text NOT NULL,
                                           date bigint DEFAULT '0'::bigint NOT NULL
);


ALTER TABLE campfire_db.units_history OWNER TO postgres;

--
-- Name: units_history_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.units_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.units_history_id_seq OWNER TO postgres;

--
-- Name: units_history_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.units_history_id_seq OWNED BY campfire_db.units_history.id;


--
-- Name: units_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.units_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.units_id_seq OWNER TO postgres;

--
-- Name: units_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.units_id_seq OWNED BY campfire_db.units.id;


--
-- Name: units_karma_transactions; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.units_karma_transactions (
                                                      id bigint NOT NULL,
                                                      from_account_id bigint DEFAULT '0'::bigint NOT NULL,
                                                      target_account_id bigint DEFAULT '0'::bigint NOT NULL,
                                                      date_create bigint DEFAULT '0'::bigint NOT NULL,
                                                      unit_id bigint DEFAULT '0'::bigint NOT NULL,
                                                      karma_count bigint DEFAULT '0'::bigint NOT NULL,
                                                      change_account_karma boolean NOT NULL,
                                                      fandom_id bigint DEFAULT '0'::bigint NOT NULL,
                                                      language_id bigint DEFAULT '0'::bigint NOT NULL,
                                                      karma_cof bigint DEFAULT '0'::bigint NOT NULL,
                                                      anonymous bigint DEFAULT '0'::bigint NOT NULL
);


ALTER TABLE campfire_db.units_karma_transactions OWNER TO postgres;

--
-- Name: units_karma_transactions_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.units_karma_transactions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.units_karma_transactions_id_seq OWNER TO postgres;

--
-- Name: units_karma_transactions_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.units_karma_transactions_id_seq OWNED BY campfire_db.units_karma_transactions.id;


--
-- Name: wiki_items; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.wiki_items (
                                        id bigint NOT NULL,
                                        parent_item_id bigint DEFAULT '0'::bigint NOT NULL,
                                        fandom_id bigint DEFAULT '0'::bigint NOT NULL,
                                        language_id bigint DEFAULT '0'::bigint NOT NULL,
                                        date_create bigint DEFAULT '0'::bigint NOT NULL,
                                        creator_id bigint DEFAULT '0'::bigint NOT NULL,
                                        type bigint DEFAULT '0'::bigint NOT NULL,
                                        status bigint DEFAULT '0'::bigint NOT NULL
);


ALTER TABLE campfire_db.wiki_items OWNER TO postgres;

--
-- Name: wiki_items_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.wiki_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.wiki_items_id_seq OWNER TO postgres;

--
-- Name: wiki_items_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.wiki_items_id_seq OWNED BY campfire_db.wiki_items.id;


--
-- Name: wiki_pages; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.wiki_pages (
                                        id bigint NOT NULL,
                                        item_id bigint DEFAULT '0'::bigint NOT NULL,
                                        item_data text NOT NULL,
                                        date_create bigint DEFAULT '0'::bigint NOT NULL,
                                        creator_id bigint DEFAULT '0'::bigint NOT NULL,
                                        language_id bigint DEFAULT '0'::bigint NOT NULL,
                                        event_type bigint DEFAULT '0'::bigint NOT NULL,
                                        wiki_status bigint DEFAULT '0'::bigint NOT NULL
);


ALTER TABLE campfire_db.wiki_pages OWNER TO postgres;

--
-- Name: wiki_pages_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.wiki_pages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.wiki_pages_id_seq OWNER TO postgres;

--
-- Name: wiki_pages_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.wiki_pages_id_seq OWNED BY campfire_db.wiki_pages.id;


--
-- Name: wiki_titles; Type: TABLE; Schema: campfire_db; Owner: postgres
--

CREATE TABLE campfire_db.wiki_titles (
                                         id bigint NOT NULL,
                                         item_id bigint DEFAULT '0'::bigint NOT NULL,
                                         parent_item_id bigint DEFAULT '0'::bigint NOT NULL,
                                         fandom_id bigint DEFAULT '0'::bigint NOT NULL,
                                         item_data text NOT NULL,
                                         date_create bigint DEFAULT '0'::bigint NOT NULL,
                                         type bigint DEFAULT '0'::bigint NOT NULL,
                                         creator_id bigint DEFAULT '0'::bigint NOT NULL,
                                         wiki_status bigint DEFAULT '0'::bigint NOT NULL,
                                         priority integer DEFAULT 0 NOT NULL
);


ALTER TABLE campfire_db.wiki_titles OWNER TO postgres;

--
-- Name: wiki_titles_id_seq; Type: SEQUENCE; Schema: campfire_db; Owner: postgres
--

CREATE SEQUENCE campfire_db.wiki_titles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE campfire_db.wiki_titles_id_seq OWNER TO postgres;

--
-- Name: wiki_titles_id_seq; Type: SEQUENCE OWNED BY; Schema: campfire_db; Owner: postgres
--

ALTER SEQUENCE campfire_db.wiki_titles_id_seq OWNED BY campfire_db.wiki_titles.id;


--
-- Name: pages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.pages (
    json_object_field json
);


ALTER TABLE public.pages OWNER TO postgres;

--
-- Name: accounts id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.accounts ALTER COLUMN id SET DEFAULT nextval('campfire_db.accounts_id_seq'::regclass);


--
-- Name: accounts_achievements id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.accounts_achievements ALTER COLUMN id SET DEFAULT nextval('campfire_db.accounts_achievements_id_seq'::regclass);


--
-- Name: accounts_effects id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.accounts_effects ALTER COLUMN id SET DEFAULT nextval('campfire_db.accounts_effects_id_seq'::regclass);


--
-- Name: accounts_emails id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.accounts_emails ALTER COLUMN id SET DEFAULT nextval('campfire_db.accounts_emails_id_seq'::regclass);


--
-- Name: accounts_enters id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.accounts_enters ALTER COLUMN id SET DEFAULT nextval('campfire_db.accounts_enters_id_seq'::regclass);


--
-- Name: accounts_notifications id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.accounts_notifications ALTER COLUMN id SET DEFAULT nextval('campfire_db.accounts_notifications_id_seq'::regclass);


--
-- Name: activities id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.activities ALTER COLUMN id SET DEFAULT nextval('campfire_db.activities_id_seq'::regclass);


--
-- Name: activities_collisions id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.activities_collisions ALTER COLUMN id SET DEFAULT nextval('campfire_db.activities_collisions_id_seq'::regclass);


--
-- Name: chats id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.chats ALTER COLUMN id SET DEFAULT nextval('campfire_db.chats_id_seq'::regclass);


--
-- Name: chats_subscriptions id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.chats_subscriptions ALTER COLUMN id SET DEFAULT nextval('campfire_db.chats_subscriptions_id_seq'::regclass);


--
-- Name: collisions id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.collisions ALTER COLUMN id SET DEFAULT nextval('campfire_db.collisions_id_seq'::regclass);


--
-- Name: donate id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.donate ALTER COLUMN id SET DEFAULT nextval('campfire_db.donate_id_seq'::regclass);


--
-- Name: fandoms id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.fandoms ALTER COLUMN id SET DEFAULT nextval('campfire_db.fandoms_id_seq'::regclass);


--
-- Name: quest_parts id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.quest_parts ALTER COLUMN id SET DEFAULT nextval('campfire_db.quest_parts_id_seq'::regclass);


--
-- Name: rubrics id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.rubrics ALTER COLUMN id SET DEFAULT nextval('campfire_db.rubrics_id_seq'::regclass);


--
-- Name: support id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.support ALTER COLUMN id SET DEFAULT nextval('campfire_db.support_id_seq'::regclass);


--
-- Name: translates id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.translates ALTER COLUMN id SET DEFAULT nextval('campfire_db.translates_id_seq'::regclass);


--
-- Name: translates_history id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.translates_history ALTER COLUMN id SET DEFAULT nextval('campfire_db.translates_history_id_seq'::regclass);


--
-- Name: units id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.units ALTER COLUMN id SET DEFAULT nextval('campfire_db.units_id_seq'::regclass);


--
-- Name: units_history id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.units_history ALTER COLUMN id SET DEFAULT nextval('campfire_db.units_history_id_seq'::regclass);


--
-- Name: units_karma_transactions id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.units_karma_transactions ALTER COLUMN id SET DEFAULT nextval('campfire_db.units_karma_transactions_id_seq'::regclass);


--
-- Name: wiki_items id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.wiki_items ALTER COLUMN id SET DEFAULT nextval('campfire_db.wiki_items_id_seq'::regclass);


--
-- Name: wiki_pages id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.wiki_pages ALTER COLUMN id SET DEFAULT nextval('campfire_db.wiki_pages_id_seq'::regclass);


--
-- Name: wiki_titles id; Type: DEFAULT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.wiki_titles ALTER COLUMN id SET DEFAULT nextval('campfire_db.wiki_titles_id_seq'::regclass);


--
-- Name: accounts_firebase accounts_firebase_firebase_uid_key; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.accounts_firebase
    ADD CONSTRAINT accounts_firebase_firebase_uid_key UNIQUE (firebase_uid);


--
-- Name: accounts idx_18157_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.accounts
    ADD CONSTRAINT idx_18157_primary PRIMARY KEY (id);


--
-- Name: accounts_achievements idx_18180_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.accounts_achievements
    ADD CONSTRAINT idx_18180_primary PRIMARY KEY (id);


--
-- Name: accounts_effects idx_18188_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.accounts_effects
    ADD CONSTRAINT idx_18188_primary PRIMARY KEY (id);


--
-- Name: accounts_emails idx_18202_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.accounts_emails
    ADD CONSTRAINT idx_18202_primary PRIMARY KEY (id);


--
-- Name: accounts_enters idx_18212_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.accounts_enters
    ADD CONSTRAINT idx_18212_primary PRIMARY KEY (id);


--
-- Name: accounts_notifications idx_18219_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.accounts_notifications
    ADD CONSTRAINT idx_18219_primary PRIMARY KEY (id);


--
-- Name: activities idx_18230_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.activities
    ADD CONSTRAINT idx_18230_primary PRIMARY KEY (id);


--
-- Name: activities_collisions idx_18251_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.activities_collisions
    ADD CONSTRAINT idx_18251_primary PRIMARY KEY (id);


--
-- Name: chats idx_18268_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.chats
    ADD CONSTRAINT idx_18268_primary PRIMARY KEY (id);


--
-- Name: chats_subscriptions idx_18284_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.chats_subscriptions
    ADD CONSTRAINT idx_18284_primary PRIMARY KEY (id);


--
-- Name: collisions idx_18303_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.collisions
    ADD CONSTRAINT idx_18303_primary PRIMARY KEY (id);


--
-- Name: donate idx_18320_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.donate
    ADD CONSTRAINT idx_18320_primary PRIMARY KEY (id);


--
-- Name: fandoms idx_18330_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.fandoms
    ADD CONSTRAINT idx_18330_primary PRIMARY KEY (id);


--
-- Name: rubrics idx_18345_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.rubrics
    ADD CONSTRAINT idx_18345_primary PRIMARY KEY (id);


--
-- Name: support idx_18361_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.support
    ADD CONSTRAINT idx_18361_primary PRIMARY KEY (id);


--
-- Name: translates idx_18374_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.translates
    ADD CONSTRAINT idx_18374_primary PRIMARY KEY (id);


--
-- Name: translates_history idx_18385_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.translates_history
    ADD CONSTRAINT idx_18385_primary PRIMARY KEY (id);


--
-- Name: units idx_18402_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.units
    ADD CONSTRAINT idx_18402_primary PRIMARY KEY (id);


--
-- Name: units_history idx_18433_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.units_history
    ADD CONSTRAINT idx_18433_primary PRIMARY KEY (id);


--
-- Name: units_karma_transactions idx_18443_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.units_karma_transactions
    ADD CONSTRAINT idx_18443_primary PRIMARY KEY (id);


--
-- Name: wiki_items idx_18457_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.wiki_items
    ADD CONSTRAINT idx_18457_primary PRIMARY KEY (id);


--
-- Name: wiki_pages idx_18469_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.wiki_pages
    ADD CONSTRAINT idx_18469_primary PRIMARY KEY (id);


--
-- Name: wiki_titles idx_18482_primary; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.wiki_titles
    ADD CONSTRAINT idx_18482_primary PRIMARY KEY (id);


--
-- Name: quest_parts quest_parts_pkey; Type: CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.quest_parts
    ADD CONSTRAINT quest_parts_pkey PRIMARY KEY (id);


--
-- Name: accounts_effects_account_id_idx; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX accounts_effects_account_id_idx ON campfire_db.accounts_effects USING btree (account_id);


--
-- Name: idx_18157_google_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18157_google_id ON campfire_db.accounts USING btree (google_id);


--
-- Name: idx_18157_last_online_time; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18157_last_online_time ON campfire_db.accounts USING btree (last_online_time);


--
-- Name: idx_18157_name; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18157_name ON campfire_db.accounts USING btree (name);


--
-- Name: idx_18157_refresh_token; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18157_refresh_token ON campfire_db.accounts USING btree (refresh_token);


--
-- Name: idx_18157_reports_count; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18157_reports_count ON campfire_db.accounts USING btree (reports_count);


--
-- Name: idx_18219_account_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18219_account_id ON campfire_db.accounts_notifications USING btree (account_id);


--
-- Name: idx_18219_date_create; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18219_date_create ON campfire_db.accounts_notifications USING btree (date_create);


--
-- Name: idx_18219_notification_status; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18219_notification_status ON campfire_db.accounts_notifications USING btree (notification_status);


--
-- Name: idx_18230_creator_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18230_creator_id ON campfire_db.activities USING btree (creator_id);


--
-- Name: idx_18230_date_create; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18230_date_create ON campfire_db.activities USING btree (date_create);


--
-- Name: idx_18230_fandom_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18230_fandom_id ON campfire_db.activities USING btree (fandom_id);


--
-- Name: idx_18230_language_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18230_language_id ON campfire_db.activities USING btree (language_id);


--
-- Name: idx_18230_name; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18230_name ON campfire_db.activities USING btree (name);


--
-- Name: idx_18230_tag_1; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18230_tag_1 ON campfire_db.activities USING btree (tag_1);


--
-- Name: idx_18230_tag_2; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18230_tag_2 ON campfire_db.activities USING btree (tag_2);


--
-- Name: idx_18230_tag_3; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18230_tag_3 ON campfire_db.activities USING btree (tag_3);


--
-- Name: idx_18230_tag_s_1; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18230_tag_s_1 ON campfire_db.activities USING btree (tag_s_1);


--
-- Name: idx_18230_tag_s_2; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18230_tag_s_2 ON campfire_db.activities USING btree (tag_s_2);


--
-- Name: idx_18230_tag_s_3; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18230_tag_s_3 ON campfire_db.activities USING btree (tag_s_3);


--
-- Name: idx_18230_type; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18230_type ON campfire_db.activities USING btree (type);


--
-- Name: idx_18251_account_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18251_account_id ON campfire_db.activities_collisions USING btree (account_id);


--
-- Name: idx_18251_activity_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18251_activity_id ON campfire_db.activities_collisions USING btree (activity_id);


--
-- Name: idx_18251_date_create; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18251_date_create ON campfire_db.activities_collisions USING btree (date_create);


--
-- Name: idx_18251_tag_1; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18251_tag_1 ON campfire_db.activities_collisions USING btree (tag_1);


--
-- Name: idx_18251_tag_2; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18251_tag_2 ON campfire_db.activities_collisions USING btree (tag_2);


--
-- Name: idx_18251_tag_3; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18251_tag_3 ON campfire_db.activities_collisions USING btree (tag_3);


--
-- Name: idx_18251_type; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18251_type ON campfire_db.activities_collisions USING btree (type);


--
-- Name: idx_18268_creator_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18268_creator_id ON campfire_db.chats USING btree (creator_id);


--
-- Name: idx_18268_date_create; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18268_date_create ON campfire_db.chats USING btree (date_create);


--
-- Name: idx_18268_fandom_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18268_fandom_id ON campfire_db.chats USING btree (fandom_id);


--
-- Name: idx_18268_image_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18268_image_id ON campfire_db.chats USING btree (image_id);


--
-- Name: idx_18268_language_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18268_language_id ON campfire_db.chats USING btree (language_id);


--
-- Name: idx_18268_name; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18268_name ON campfire_db.chats USING btree (name);


--
-- Name: idx_18268_type; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18268_type ON campfire_db.chats USING btree (type);


--
-- Name: idx_18284_account_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18284_account_id ON campfire_db.chats_subscriptions USING btree (account_id);


--
-- Name: idx_18284_chat_type; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18284_chat_type ON campfire_db.chats_subscriptions USING btree (chat_type);


--
-- Name: idx_18284_enter_date; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18284_enter_date ON campfire_db.chats_subscriptions USING btree (enter_date);


--
-- Name: idx_18284_exit_date; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18284_exit_date ON campfire_db.chats_subscriptions USING btree (exit_date);


--
-- Name: idx_18284_last_message_date; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18284_last_message_date ON campfire_db.chats_subscriptions USING btree (last_message_date);


--
-- Name: idx_18284_last_message_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18284_last_message_id ON campfire_db.chats_subscriptions USING btree (last_message_id);


--
-- Name: idx_18284_member_level; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18284_member_level ON campfire_db.chats_subscriptions USING btree (member_level);


--
-- Name: idx_18284_member_owner; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18284_member_owner ON campfire_db.chats_subscriptions USING btree (member_owner);


--
-- Name: idx_18284_member_status; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18284_member_status ON campfire_db.chats_subscriptions USING btree (member_status);


--
-- Name: idx_18284_read_date; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18284_read_date ON campfire_db.chats_subscriptions USING btree (read_date);


--
-- Name: idx_18284_subscribed; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18284_subscribed ON campfire_db.chats_subscriptions USING btree (subscribed);


--
-- Name: idx_18284_target_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18284_target_id ON campfire_db.chats_subscriptions USING btree (target_id);


--
-- Name: idx_18284_target_sub_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18284_target_sub_id ON campfire_db.chats_subscriptions USING btree (target_sub_id);


--
-- Name: idx_18303_collision_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18303_collision_id ON campfire_db.collisions USING btree (collision_id);


--
-- Name: idx_18303_collision_key; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18303_collision_key ON campfire_db.collisions USING btree (collision_key);


--
-- Name: idx_18303_collision_sub_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18303_collision_sub_id ON campfire_db.collisions USING btree (collision_sub_id);


--
-- Name: idx_18303_collision_type; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18303_collision_type ON campfire_db.collisions USING btree (collision_type);


--
-- Name: idx_18303_owner + collision; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX "idx_18303_owner + collision" ON campfire_db.collisions USING btree (owner_id, collision_type);


--
-- Name: idx_18303_owner_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18303_owner_id ON campfire_db.collisions USING btree (owner_id);


--
-- Name: idx_18303_value_1; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18303_value_1 ON campfire_db.collisions USING btree (value_1);


--
-- Name: idx_18303_value_2; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18303_value_2 ON campfire_db.collisions USING btree (value_2);


--
-- Name: idx_18320_account_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18320_account_id ON campfire_db.donate USING btree (account_id);


--
-- Name: idx_18320_key; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18320_key ON campfire_db.donate USING btree (donate_key);


--
-- Name: idx_18330_creator_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18330_creator_id ON campfire_db.fandoms USING btree (creator_id);


--
-- Name: idx_18330_date_create; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18330_date_create ON campfire_db.fandoms USING btree (date_create);


--
-- Name: idx_18330_fandom_category; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18330_fandom_category ON campfire_db.fandoms USING btree (fandom_category);


--
-- Name: idx_18330_fandom_closed; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18330_fandom_closed ON campfire_db.fandoms USING btree (fandom_closed);


--
-- Name: idx_18330_image_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18330_image_id ON campfire_db.fandoms USING btree (image_id);


--
-- Name: idx_18330_karma_cof; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18330_karma_cof ON campfire_db.fandoms USING btree (karma_cof);


--
-- Name: idx_18330_name; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18330_name ON campfire_db.fandoms USING btree (name);


--
-- Name: idx_18330_status; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18330_status ON campfire_db.fandoms USING btree (status);


--
-- Name: idx_18330_subscribers_count; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18330_subscribers_count ON campfire_db.fandoms USING btree (subscribers_count);


--
-- Name: idx_18345_creator_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18345_creator_id ON campfire_db.rubrics USING btree (creator_id);


--
-- Name: idx_18345_fandom_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18345_fandom_id ON campfire_db.rubrics USING btree (fandom_id);


--
-- Name: idx_18345_fandom_id_2; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18345_fandom_id_2 ON campfire_db.rubrics USING btree (fandom_id, language_id);


--
-- Name: idx_18345_language_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18345_language_id ON campfire_db.rubrics USING btree (language_id);


--
-- Name: idx_18345_owner_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18345_owner_id ON campfire_db.rubrics USING btree (owner_id);


--
-- Name: idx_18345_status; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18345_status ON campfire_db.rubrics USING btree (status);


--
-- Name: idx_18345_status_change_date; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18345_status_change_date ON campfire_db.rubrics USING btree (status_change_date);


--
-- Name: idx_18361_count; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18361_count ON campfire_db.support USING btree (count);


--
-- Name: idx_18361_date; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18361_date ON campfire_db.support USING btree (date);


--
-- Name: idx_18361_date_create; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18361_date_create ON campfire_db.support USING btree (date_create);


--
-- Name: idx_18361_status; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18361_status ON campfire_db.support USING btree (status);


--
-- Name: idx_18361_user_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18361_user_id ON campfire_db.support USING btree (user_id);


--
-- Name: idx_18374_appkey; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18374_appkey ON campfire_db.translates USING btree (appkey);


--
-- Name: idx_18374_key; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18374_key ON campfire_db.translates USING btree (translate_key);


--
-- Name: idx_18374_languageid; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18374_languageid ON campfire_db.translates USING btree (languageid, translate_key);


--
-- Name: idx_18402_closed; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_closed ON campfire_db.units USING btree (closed);


--
-- Name: idx_18402_creator_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_creator_id ON campfire_db.units USING btree (creator_id);


--
-- Name: idx_18402_date_create; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_date_create ON campfire_db.units USING btree (date_create);


--
-- Name: idx_18402_fandom_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_fandom_id ON campfire_db.units USING btree (fandom_id);


--
-- Name: idx_18402_fandom_key; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_fandom_key ON campfire_db.units USING btree (fandom_key);


--
-- Name: idx_18402_important; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_important ON campfire_db.units USING btree (important);


--
-- Name: idx_18402_karma_count; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_karma_count ON campfire_db.units USING btree (karma_count);


--
-- Name: idx_18402_language_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_language_id ON campfire_db.units USING btree (language_id);


--
-- Name: idx_18402_parent_fandom_closed; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_parent_fandom_closed ON campfire_db.units USING btree (parent_fandom_closed);


--
-- Name: idx_18402_parent_unit_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_parent_unit_id ON campfire_db.units USING btree (parent_unit_id);


--
-- Name: idx_18402_status; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_status ON campfire_db.units USING btree (status);


--
-- Name: idx_18402_tag_1; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_tag_1 ON campfire_db.units USING btree (tag_1);


--
-- Name: idx_18402_tag_1_2; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_tag_1_2 ON campfire_db.units USING btree (tag_1, tag_2, unit_type);


--
-- Name: idx_18402_tag_2; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_tag_2 ON campfire_db.units USING btree (tag_2);


--
-- Name: idx_18402_tag_3; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_tag_3 ON campfire_db.units USING btree (tag_3);


--
-- Name: idx_18402_tag_4; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_tag_4 ON campfire_db.units USING btree (tag_4);


--
-- Name: idx_18402_tag_6; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_tag_6 ON campfire_db.units USING btree (tag_6);


--
-- Name: idx_18402_tag_7; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_tag_7 ON campfire_db.units USING btree (tag_7);


--
-- Name: idx_18402_tag_s_1; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_tag_s_1 ON campfire_db.units USING btree (tag_s_1);


--
-- Name: idx_18402_tag_s_2; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_tag_s_2 ON campfire_db.units USING btree (tag_s_2);


--
-- Name: idx_18402_unit_reports_count; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_unit_reports_count ON campfire_db.units USING btree (unit_reports_count);


--
-- Name: idx_18402_unit_type; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_unit_type ON campfire_db.units USING btree (unit_type);


--
-- Name: idx_18402_unit_type_2; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18402_unit_type_2 ON campfire_db.units USING btree (unit_type, date_create);


--
-- Name: idx_18433_date; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18433_date ON campfire_db.units_history USING btree (date);


--
-- Name: idx_18433_history_type; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18433_history_type ON campfire_db.units_history USING btree (history_type);


--
-- Name: idx_18433_unit_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18433_unit_id ON campfire_db.units_history USING btree (unit_id);


--
-- Name: idx_18443_anonymous; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18443_anonymous ON campfire_db.units_karma_transactions USING btree (anonymous);


--
-- Name: idx_18443_change_account_karma; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18443_change_account_karma ON campfire_db.units_karma_transactions USING btree (change_account_karma);


--
-- Name: idx_18443_date_create; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18443_date_create ON campfire_db.units_karma_transactions USING btree (date_create);


--
-- Name: idx_18443_fandom_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18443_fandom_id ON campfire_db.units_karma_transactions USING btree (fandom_id);


--
-- Name: idx_18443_from_account_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18443_from_account_id ON campfire_db.units_karma_transactions USING btree (from_account_id);


--
-- Name: idx_18443_language_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18443_language_id ON campfire_db.units_karma_transactions USING btree (language_id);


--
-- Name: idx_18443_target_account_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18443_target_account_id ON campfire_db.units_karma_transactions USING btree (target_account_id);


--
-- Name: idx_18443_unit_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18443_unit_id ON campfire_db.units_karma_transactions USING btree (unit_id);


--
-- Name: idx_18457_date_create; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18457_date_create ON campfire_db.wiki_items USING btree (date_create);


--
-- Name: idx_18457_fandom_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18457_fandom_id ON campfire_db.wiki_items USING btree (fandom_id);


--
-- Name: idx_18457_language_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18457_language_id ON campfire_db.wiki_items USING btree (language_id);


--
-- Name: idx_18457_parent_item_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18457_parent_item_id ON campfire_db.wiki_items USING btree (parent_item_id);


--
-- Name: idx_18457_type; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18457_type ON campfire_db.wiki_items USING btree (type);


--
-- Name: idx_18469_creator_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18469_creator_id ON campfire_db.wiki_pages USING btree (creator_id);


--
-- Name: idx_18469_date_create; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18469_date_create ON campfire_db.wiki_pages USING btree (date_create);


--
-- Name: idx_18469_item_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18469_item_id ON campfire_db.wiki_pages USING btree (item_id);


--
-- Name: idx_18469_language_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18469_language_id ON campfire_db.wiki_pages USING btree (language_id);


--
-- Name: idx_18469_status; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18469_status ON campfire_db.wiki_pages USING btree (wiki_status);


--
-- Name: idx_18482_date_create; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18482_date_create ON campfire_db.wiki_titles USING btree (date_create);


--
-- Name: idx_18482_item_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18482_item_id ON campfire_db.wiki_titles USING btree (item_id);


--
-- Name: idx_18482_parent_item_id; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18482_parent_item_id ON campfire_db.wiki_titles USING btree (parent_item_id);


--
-- Name: idx_18482_status; Type: INDEX; Schema: campfire_db; Owner: postgres
--

CREATE INDEX idx_18482_status ON campfire_db.wiki_titles USING btree (wiki_status);


--
-- Name: accounts_firebase accounts_firebase_account_id_fkey; Type: FK CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.accounts_firebase
    ADD CONSTRAINT accounts_firebase_account_id_fkey FOREIGN KEY (account_id) REFERENCES campfire_db.accounts(id);


--
-- Name: quest_parts quest_parts_unit_id_fkey; Type: FK CONSTRAINT; Schema: campfire_db; Owner: postgres
--

ALTER TABLE ONLY campfire_db.quest_parts
    ADD CONSTRAINT quest_parts_unit_id_fkey FOREIGN KEY (unit_id) REFERENCES campfire_db.units(id);


--
-- PostgreSQL database dump complete
--

alter schema campfire_db rename to public;
