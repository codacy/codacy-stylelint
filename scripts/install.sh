#!/usr/bin/env bash

#set -e

TOOL_VERSION=$(cat .stylelint-version)

npm install -g stylelint@${TOOL_VERSION}
npm install -g stylelint-config-recommended@3.0.0
npm install -g stylelint-config-standard@19.0.0
npm install -g stylelint-order@3.1.1
npm install -g stylelint-suitcss@3.0.0
npm install -g stylelint-config-suitcss@14.0.0
npm install -g stylelint-scss@3.11.1
npm install -g stylelint-config-standard-scss@1.1.0
npm install -g stylelint-config-recommended-scss@4.0.0
npm install -g stylelint-config-wordpress@14.0.0
npm install -g stylelint-csstree-validator@1.5.2
npm install -g stylelint-declaration-strict-value@1.1.6
npm install -g stylelint-declaration-use-variable@1.7.0
npm install -g stylelint-rscss@0.4.0
npm install -g stylelint-selector-bem-pattern@2.1.0
npm install -g stylelint-config-slds@1.0.7
npm install -g stylelint-config-prettier@6.0.0
npm install -g stylelint-config-css-modules@1.5.0
npm install -g prettier@1.18.2
npm install -g stylelint-prettier@1.1.1
npm install -g stylelint-config-styled-components@0.1.1
npm install -g stylelint-processor-styled-components@1.8.0
npm install -g stylelint-a11y@1.2.2
