# ADR-001: Project Vision

**Status:** Accepted

**Date:** 2026-07-01

---

# Context

Managing Linux servers from a mobile device is often inefficient.

Existing Android applications usually focus on terminal access and provide little support for infrastructure management workflows.

System administrators frequently need to switch between multiple applications to perform common operational tasks such as:

- Managing server inventory
- Monitoring server availability
- Executing predefined maintenance commands
- Managing Xray services
- Organizing production and testing environments
- Accessing frequently used servers

The project requires a clear product direction before making architectural decisions.

---

# Decision

Server Toolkit will be developed as an infrastructure management application rather than a traditional SSH client.

SSH is considered one capability of the application, not its primary purpose.

The project will prioritize operational efficiency, maintainability, and a modern Android user experience.

The application will follow modern Android development practices and production-quality engineering standards from the beginning of the project.

---

# Alternatives Considered

## Traditional SSH Client

Build a lightweight terminal application similar to existing SSH clients.

### Pros

- Small application
- Fast development
- Lower maintenance cost

### Cons

- Limited functionality
- No infrastructure management
- Little differentiation from existing applications

Rejected.

---

## Infrastructure Management Application

Create a management platform focused on operational workflows.

### Pros

- Higher productivity
- Better user experience
- Scalable architecture
- Clear long-term vision
- Suitable as a professional portfolio project

### Cons

- Larger project scope
- Longer development time
- Higher architectural complexity

Accepted.

---

# Consequences

## Positive

- Clear product direction.
- Better feature prioritization.
- Consistent architectural decisions.
- Improved long-term maintainability.
- Strong foundation for future expansion.

## Negative

- Increased implementation effort.
- More comprehensive documentation required.
- Longer initial development phase.

---

# Future Considerations

Possible future capabilities include:

- Docker management
- Container monitoring
- WireGuard management
- Tailscale integration
- Kubernetes support
- Cloud provider integration
- Notification system
- Background monitoring
- Secure synchronization across devices

These features are intentionally outside the scope of version 1.0.

---

# References

- PRODUCT_VISION.md
- ARCHITECTURE.md
- DEVELOPMENT.md
- ROADMAP.md

---

# Notes

This ADR establishes the long-term vision of the project.

Future architecture decisions should remain aligned with this document.

If the project vision changes significantly, a new ADR should be created instead of modifying this one.