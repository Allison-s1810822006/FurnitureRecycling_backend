-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.app_users (
user_id uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
full_name character varying,
password_hash character varying,
is_admin boolean DEFAULT false,
email character varying,
phone character varying,
address character varying,
line_user_id character varying UNIQUE,
line_display_name character varying,
line_picture_url character varying,
line_bound_at timestamp with time zone,
line_email character varying,
CONSTRAINT app_users_pkey PRIMARY KEY (user_id)
);
CREATE TABLE public.application_items (
item_id uuid NOT NULL DEFAULT gen_random_uuid(),
application_id uuid NOT NULL,
item_name character varying,
quantity integer NOT NULL DEFAULT 1,
photos text DEFAULT ARRAY[]::text[],
created_at timestamp with time zone NOT NULL DEFAULT now(),
updated_at timestamp with time zone NOT NULL DEFAULT now(),
furniture_item_id integer,
CONSTRAINT application_items_pkey PRIMARY KEY (item_id),
CONSTRAINT application_items_application_id_fkey FOREIGN KEY (application_id) REFERENCES public.applications(application_id),
CONSTRAINT application_items_furniture_item_id_fkey FOREIGN KEY (furniture_item_id) REFERENCES public.furniture(item_id)
);
CREATE TABLE public.applications (
application_id uuid NOT NULL DEFAULT gen_random_uuid(),
user_id uuid NOT NULL,
station_id character varying,
requested_date date,
status character varying DEFAULT 'pending'::text,
created_at timestamp with time zone DEFAULT now(),
updated_at timestamp with time zone DEFAULT now(),
total_items integer DEFAULT 0,
total_volume_m3 numeric DEFAULT 0,
suggested_vehicle character varying DEFAULT 'FLATBED'::text,
schedule_id uuid,
sequence_no integer,
CONSTRAINT applications_pkey PRIMARY KEY (application_id),
CONSTRAINT applications_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_users(user_id),
CONSTRAINT fk8ni55936prbj46uk00lfvi7pv FOREIGN KEY (schedule_id) REFERENCES public.schedules(schedule_id),
CONSTRAINT applications_station_id_fkey FOREIGN KEY (station_id) REFERENCES public.stations(station_id)
);
CREATE TABLE public.furniture (
item_name character varying NOT NULL,
length_m double precision NOT NULL CHECK (length_m >= 0::numeric::double precision),
width_m double precision NOT NULL CHECK (width_m >= 0::numeric::double precision),
height_m double precision NOT NULL CHECK (height_m >= 0::numeric::double precision),
type character varying NOT NULL,
item_id integer NOT NULL UNIQUE,
CONSTRAINT furniture_pkey PRIMARY KEY (item_id)
);
CREATE TABLE public.schedules (
schedule_id uuid NOT NULL DEFAULT gen_random_uuid(),
schedule_date date NOT NULL,
applications_count integer NOT NULL DEFAULT 0,
eta timestamp with time zone NOT NULL,
plate_number character varying NOT NULL,
CONSTRAINT schedules_pkey PRIMARY KEY (schedule_id),
CONSTRAINT schedules_plate_number_fkey FOREIGN KEY (plate_number) REFERENCES public.vehicles(plate_number)
);
CREATE TABLE public.stations (
station_id character varying NOT NULL DEFAULT gen_random_uuid(),
name character varying NOT NULL,
address character varying NOT NULL,
amount smallint NOT NULL,
CONSTRAINT stations_pkey PRIMARY KEY (station_id)
);
CREATE TABLE public.vehicles (
plate_number text NOT NULL UNIQUE,
driver_name text,
last_inspection_date date,
next_inspection_date date,
last_maintenance_date date,
box_length double precision NOT NULL,
box_width double precision NOT NULL,
box_height double precision NOT NULL,
CONSTRAINT vehicles_pkey PRIMARY KEY (plate_number)
);