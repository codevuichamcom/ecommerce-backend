---
description: Dependency update workflow - security, compatibility, and impact
---

# Dependency Update Workflow

## Overview
This workflow guides the process of adding, updating, or removing dependencies while ensuring security and stability.

---

## Steps

### 1. Identify Need
- **New Feature**: Requires a library not currently in the project.
- **Security Patch**: Updating due to a vulnerability (e.g., from Snyk/OWASP).
- **Maintenance**: Regular update to stay current with LTS versions.

### 2. Research & Evaluate
- **License Check**: Must be compatible with the project (e.g., Apache 2.0, MIT). **Avoid LGPL/GPL** unless approved.
- **Maintenance Status**: Is the library actively maintained? Check GitHub stars, last commit, and community.
- **Security History**: Check for known vulnerabilities.
- **Performance Impact**: Does it add Significant binary size or slow down startup?
- **Alternatives**: Are there better or existing libraries in the project that can do the job?

### 3. Human Review ⏸️
**STOP HERE - Get approval for NEW dependencies**
- Present the library, its purpose, and the research from Step 2.
- For simple version updates of existing libs, this step can be skipped unless it's a major version.

### 4. Implementation
- **Add to `build.gradle.kts`**:
  - Use specific versions (avoid `+` or `latest`).
  - Add to the correct module.
- **Run Security Scan**:
  ```bash
  ./gradlew dependencyCheckAnalyze
  ```
- **Check for Conflicts**:
  ```bash
  ./gradlew dependencies
  ```

### 5. Verification
- **Run All Tests**: Ensure no regressions in existing functionality.
- **Check Startup Time**: Verify no significant degradation.
- **Monitor Memory/CPU**: Brief check of resource usage.

### 6. PR & Documentation
- Document why the dependency was added/updated.
- Include the security scan report in the PR if it was a security patch.

---

## Example Checklist

- [ ] Need identified and documented
- [ ] License compatibility verified
- [ ] Maintenance status checked
- [ ] Human approval received (for new libs)
- [ ] Security scan passed
- [ ] No dependency conflicts
- [ ] All tests passing
- [ ] PR created with justification
