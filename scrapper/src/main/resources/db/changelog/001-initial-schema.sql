CREATE TABLE IF NOT EXISTS link_table (
          link_id BIGSERIAL PRIMARY KEY,
          link_url TEXT NOT NULL UNIQUE,
          last_check_time TIMESTAMP WITH TIME ZONE,
          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP);


CREATE TABLE IF NOT EXISTS tag_table (
     tag_id BIGSERIAL PRIMARY KEY,
     tag VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS link_tag_table (
      link_id BIGINT REFERENCES link_table(link_id) ON DELETE CASCADE,
      tag_id BIGINT REFERENCES tag_table(tag_id) ON DELETE CASCADE,
      PRIMARY KEY (link_id, tag_id)
);
CREATE TABLE if not exists chat_table (
    chat_id BIGSERIAL PRIMARY KEY
);


CREATE TABLE IF NOT EXISTS chat_link_table (
       chat_id BIGINT REFERENCES chat_table(chat_id) ON DELETE CASCADE ,
       link_id BIGINT REFERENCES link_table(link_id) ON DELETE CASCADE,
       PRIMARY KEY (chat_id, link_id)
);

