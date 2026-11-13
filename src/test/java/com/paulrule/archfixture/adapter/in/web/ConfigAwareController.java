package com.paulrule.archfixture.adapter.in.web;

import com.paulrule.archfixture.config.FixtureConfig;

// Deliberate violation: proves ArchitectureTest's adapter rule can fail.
public class ConfigAwareController {
    FixtureConfig config = new FixtureConfig();
}
