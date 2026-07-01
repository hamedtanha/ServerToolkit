# ADR-001 Project Vision

Status: Accepted

Date: 2026-07-01

## Context

Server administrators and DevOps engineers often need to perform quick maintenance tasks from a mobile device.

Existing mobile SSH applications provide terminal access but usually lack infrastructure-oriented management features such as server inventory, monitoring, Xray management, certificate renewal, and operational dashboards.

The goal of Server Toolkit is to provide a modern Android application focused on infrastructure management instead of being only an SSH client.

## Decision

The application will be designed as an infrastructure management tool.

SSH connectivity is only one feature of the application.

The application will focus on productivity, operational visibility, and fast access to frequently used server management tasks.

## Alternatives Considered

### Generic SSH Client

Only provide SSH terminal access.

Rejected because it does not solve infrastructure management problems.

### Remote Desktop Style Application

Provide complete remote administration.

Rejected because it increases complexity and is outside the scope of the project.

### Infrastructure Management Application

Provide server inventory, monitoring, SSH, automation, and operational tools.

Accepted.

## Consequences

### Positive

- Clear long-term vision.
- Easier feature prioritization.
- Better architecture decisions.
- Suitable as a professional portfolio project.

### Negative

- Larger project scope.
- Longer development time.

### Future Considerations

Possible future features include:

- Cluster management
- Docker management
- Kubernetes support
- WireGuard management
- Tailscale integration
- Cloud provider integration