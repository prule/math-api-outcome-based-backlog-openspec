package com.paulrule.archfixture.application;

import org.springframework.stereotype.Service;

// Deliberate violation: proves ArchitectureTest's application rule can fail.
@Service
public class SpringAwareUseCase {}
