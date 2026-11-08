CREATE TABLE IF NOT EXISTS tool_calls (
  tool_use_id VARCHAR(100) PRIMARY KEY,
  case_id     VARCHAR(100) NOT NULL,
  tool        VARCHAR(100) NOT NULL,
  input       VARCHAR(4000),
  decision    VARCHAR(20)  NOT NULL,
  result      VARCHAR(4000),
  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS processed_events (
  event_id VARCHAR(100) PRIMARY KEY,
  result   VARCHAR(4000)
);
