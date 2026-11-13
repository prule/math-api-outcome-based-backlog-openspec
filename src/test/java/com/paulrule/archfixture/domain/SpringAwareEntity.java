package com.paulrule.archfixture.domain;

import org.springframework.stereotype.Component;

// Deliberate violation: proves ArchitectureTest's domain rule can fail.
@Component
public class SpringAwareEntity {}
