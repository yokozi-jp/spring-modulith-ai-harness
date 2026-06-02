---
inclusion: fileMatch
fileMatchPattern: ['**/server/**', '**/services/**', '**/db/**', '**/models/**', '**/migrations/**']
description: Backend conventions (auto-loaded when working in server code)
---
# Backend Standards

## Database
[Schema migration approach, ORM/query builder]

## Error Handling
- Never leak internal errors to clients
- Log structured errors with request ID
- Use typed error classes

## Performance
- Add indexes for frequent queries
- Cache expensive operations
- Set query timeouts