// "init"

create table if not exists split (
 first_ts INTEGER PRIMARY KEY,
 track_id INTEGER,
 data BLOB NOT NULL,
 foreign key (track_id) references track(started_ts)
);

create table if not exists track (
 started_ts INTEGER PRIMARY KEY,
 name TEXT,
 status TEXT check(status in ("local", "pending", "commit")) NOT NULL DEFAULT "local",
 progress INTEGER check(progress >= 0 and (progress == 0 or status != "local")) NOT NULL DEFAULT 0,
 committed_ts INTEGER check(committed_ts > started_ts or status != "committed") NOT NULL DEFAULT 0
);


// "add track"

insert into track (started_ts, name) values(?, ?)