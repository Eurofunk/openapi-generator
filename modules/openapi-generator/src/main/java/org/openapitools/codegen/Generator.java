/*
 * Copyright 2018 OpenAPI-Generator Contributors (https://openapi-generator.tech)
 * Copyright 2018 SmartBear Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openapitools.codegen;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface Generator {
    Generator opts(ClientOptInput opts);

    List<File> generate();

    String getName();

    void setGenerateMetadata(Boolean generateMetadata);

    void displayDryRunResults(Map<String, DryRunStatus> fileStatusMap);

    boolean hasChanges();
}