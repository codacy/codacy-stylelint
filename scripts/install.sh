#!/usr/bin/env bash

set -e

TOOL_VERSION=$(cat .stylelint-version)


npm install -g stylelint@${TOOL_VERSION}
npm install -g stylelint-config-standard@18.2.0
npm install -g stylelint-config-recommended@2.1.0
npm install -g stylelint-order@0.8.1
npm install -g stylelint-suitcss@3.0.0
npm install -g stylelint-config-suitcss@14.0.0
npm install -g stylelint-scss@3.6.1
npm install -g stylelint-config-recommended-scss@3.3.0
npm install -g stylelint-config-wordpress@13.0.0
npm install -g stylelint-csstree-validator@1.3.0
npm install -g stylelint-declaration-strict-value@1.0.4
npm install -g stylelint-declaration-use-variable@1.7.0
npm install -g stylelint-rscss@0.4.0
npm install -g stylelint-selector-bem-pattern@2.0.0
npm install -g stylelint-config-slds@1.0.7
npm install -g stylelint-config-prettier@4.0.0
