# service-health Specification

## Purpose
Lets developers and operators tell, over HTTP, whether a running instance of the service is up and able to serve.

## Requirements

### Requirement: Service reports its health
The running service SHALL answer `GET /actuator/health` with HTTP 200 and a JSON body whose `status` is `UP` once it
has started and is able to serve requests.

#### Scenario: Started service is healthy
- **WHEN** the service has started and a client sends `GET /actuator/health`
- **THEN** the response status is 200
- **AND** the JSON body has `"status": "UP"`

### Requirement: Service reports liveness and readiness
The running service SHALL answer `GET /actuator/health/liveness` and `GET /actuator/health/readiness` with HTTP 200
and a JSON body whose `status` is `UP` once it has started and is able to serve requests (PRD §3.3).

#### Scenario: Started service is live and ready
- **WHEN** the service has started and a client sends `GET /actuator/health/liveness` or `GET /actuator/health/readiness`
- **THEN** the response status is 200
- **AND** the JSON body has `"status": "UP"`
