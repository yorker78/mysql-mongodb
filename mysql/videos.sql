# MySQL half of the MySQL vs. MongoDB comparison.
# Loads the video store data into a raw schema (videos), normalizes it into
# people, recordings, and worked_on (videos2), then runs the same queries as
# the MongoDB implementation. Run from the repository root with local_infile
# enabled, since the data files are loaded from src/main/resources.

drop schema if exists videos;
create schema videos default character set latin1;
use videos;

drop table if exists videos.recordings;
drop table if exists videos.categories;
drop table if exists videos.actors;

create table recordings (
	recording_id int,
    director varchar(20),
    title varchar(40),
    category varchar(20),
    image_name varchar(30),
    duration double,
    rating varchar(5),
    year_release int,
    price double,
    stock_count int
);

create table categories (
	id int,
    name varchar(20)
);

create table actors (
	id int,
    name varchar(20),
    recording_id int
);

Load Data local infile 'src/main/resources/cleaned_video_recordings.txt' into table videos.recordings fields terminated by '\t' lines terminated by '\r\n';
Load Data local infile 'src/main/resources/cleaned_video_categories.txt' into table videos.categories fields terminated by '\t' lines terminated by '\r\n';
Load Data local infile 'src/main/resources/cleaned_video_actors.txt' into table videos.actors fields terminated by '\t' lines terminated by '\r\n';

select * from videos.recordings limit 10;
select * from videos.categories limit 10;
select * from videos.actors limit 10;

# There will be a lot of exact call outs since the new schema
# will reuse some of the same names and it will be easier to
# have the full identifiers to keep everything straight.
drop schema if exists videos2;
create schema videos2 default character set latin1;
use videos2;

drop table if exists people;
drop table if exists recordings;
drop table if exists worked_on;

# Theoretically, if it were confirmed that the names
# given were SAG names, we could use that as the primary
# key since the SAG requires the name to be unique.
create table people (
	sag_name varchar(30)
) engine=INNODB;

insert into videos2.people
select distinct videos.actors.name 
	from videos.actors
	where videos.actors.name not in (select videos.recordings.director from videos.recordings)
union 
	select distinct videos.recordings.director
	from videos.recordings;



# Add id to table, auto-fill it, and make it the primary key.alter
# We want to do this after inserting everything because its faster to
# batch add everything to the table, then batch index them instead of
# adding a row and indexing it immediately.
alter table videos2.people
add column id int not null auto_increment,
add primary key (id);

create table recordings (
    title varchar(40),
    category enum("Action & Adventure","Comedy","Drama","Horror","Science Fiction","Suspense"),
    image_name varchar(30),
    duration int,
    rating enum("PG","PG-13","R","NR"),
    year_released int,
    price double,
    stock_count int
) engine=INNODB;

insert into videos2.recordings 
select distinct videos.recordings.title, videos.recordings.category, 
				videos.recordings.image_name, videos.recordings.duration, 
				videos.recordings.rating, videos.recordings.year_release, 
                videos.recordings.price, videos.recordings.stock_count 
from videos.recordings;

# Add id to table, auto-fill it, and make it the primary key.alter
# We want to do this after inserting everything because its faster to
# batch add everything to the table, then batch index them instead of
# adding a row and indexing it immediately.
alter table videos2.recordings
add column id int not null auto_increment,
add primary key (id);

# Rather than have 2 tables with the exact same relational info,
# I think it would be more effecient to simply add a column that
# contains the credit of how that person worked on the recording.
create table worked_on (
	person_id int not null,
    recording_id int not null,
    recording_role enum("Actor", "Director") not null,
    foreign key (person_id) references people(id),
    foreign key (recording_id) references recordings(id),
    primary key (person_id, recording_id, recording_role)
) engine=INNODB;

# Insert all the actors into the "worked_on" table using names and titles
# for the comparison to the new tables since we are not using the old ids 
# except for comparisons within the original database
insert into videos2.worked_on 
	select p.id, r2.id, "Actor" 
	from videos2.people p, videos2.recordings r2, videos.actors a, videos.recordings r1
	where p.sag_name = a.name
		and r2.title = r1.title
		and a.recording_id = r1.recording_id;

# Insert all the directors into the "worked_on" table and this time
# we don't use the old ids at all, just name comparisons.
insert into videos2.worked_on
	select p.id, r2.id, "Director"
	from videos2.people p, videos2.recordings r2, videos.recordings r1
	where r2.title = r1.title
		and p.sag_name = r1.director;

select * from videos2.people limit 10;
select * from videos2.recordings limit 10;

# Get the names and roles of the people who worked on movies together.
# Without the limit, I would expect multiple copies of the same name
# to appear, so grouping by the movie first should keep it looking how we want.
select p.sag_name, r.title, w.recording_role
from videos2.people p, videos2.recordings r, videos2.worked_on w
	where w.person_id = p.id 
    and w.recording_id = r.id
group by r.title, w.recording_role, p.sag_name
limit 10;

# Query 1: 330 Rows Returned
use videos;
select * from videos.recordings, videos.categories limit 10;

use videos;
select * from videos.recordings r
left outer join videos.categories c
on r.category = c.name
union
select * from videos.recordings r
right outer join videos.categories c
on r.category = c.name;

# Query 2: 55 Rows Returned
use videos;
select * from videos.recordings vr, videos.categories vc where vr.category=vc.name limit 10;

# Query 3: Number of movies in each genre
# 6 Rows Returned:
# Action & Adventure: 10
# Comedy: 10
# Drama: 10
# Horror: 8
# Science Fiction: 9
# Suspense: 8
use videos2;
select r.category, count(r.id) as "# Movies in Category"
from videos2.recordings r
group by r.category;

# Query 4: Number of in-stock movies in each genre
# 6 Rows Returned:
# Action & Adventure: 8
# Comedy: 8
# Drama: 9
# Horror: 6
# Science Fiction: 8
# Suspense: 6
use videos2;
select r.category, count(r.id) as "# Movies in Category"
from videos2.recordings r
where r.stock_count > 0
group by r.category;

# Query 5: Genres each actor has appeared in
# 335 Rows Returned
use videos2;
select a.sag_name, group_concat(distinct r.category separator ', ') as "categories"
from videos2.people a, videos2.recordings r, videos2.worked_on w
where w.recording_role = "Actor"
and a.id = w.person_id
and r.id = w.recording_id
group by a.sag_name
limit 10;

# Query 6: Actors who have appeared in more than one genre
# 25 Rows Retuned
use videos2;
select a.sag_name, group_concat(distinct r.category separator ', ') as "categories"
from videos2.people a, videos2.recordings r, videos2.recordings r2, videos2.worked_on w, videos2.worked_on w2
where w.recording_role = "Actor"
and w2.recording_role = "Actor"
and a.id = w.person_id
and r.id = w.recording_id
and a.id = w2.person_id
and r2.id = w2.recording_id
and r.category != r2.category
group by a.sag_name
limit 10;


# Query 7: Actors who have never appeared in a comedy
# 265 Rows Returned
use videos2;
select distinct a.sag_name
from videos2.people a, videos2.worked_on w, videos2.recordings r
where w.recording_role = "Actor"
and a.id = w.person_id
and r.id = w.recording_id
and a.sag_name not in (
	select a2.sag_name
	from videos2.people a2, videos2.recordings r2, videos2.worked_on w2
	where w2.recording_role = "Actor"
	and a2.id = w2.person_id
	and r2.id = w2.recording_id
    and r2.category = "Comedy"
)
limit 10;

# Query 8: Actors who have appeared in both a comedy and an action & adventure movie
# 4 Rows Returned
use videos2;
select a.sag_name
from videos2.people a, videos2.recordings r, videos2.worked_on w, videos2.worked_on w2, videos2.recordings r2
where w.recording_role = "Actor"
and w2.recording_role = "Actor"
and a.id = w.person_id
and r.id = w.recording_id
and a.id = w2.person_id
and r2.id = w2.recording_id
and r.category = "Comedy"
and r2.category = "Action & Adventure"
group by a.sag_name
limit 10;

# Query 9: Directors who had cameos in their own movies
# I think the most interesting part of this query is the
# join of worked_on to worked_on
# 3 Rows Returned
use videos2;
select p.sag_name, r.title, group_concat(w.recording_role separator ', ') as "roles"
from videos2.people p, videos2.recordings r, videos2.worked_on w, videos2.worked_on w2
	where w.person_id = p.id 
    and w.recording_id = r.id
    and w2.person_id = p.id
    and w2.recording_id = r.id
    and w.recording_role != w2.recording_role
group by w.person_id, w.recording_id
limit 10;