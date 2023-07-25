/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package net.consensys.besu.plugins.dsl.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.besu.plugin.services.PicoCLIOptions;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

@CommandLine.Command
public class CommandLineTest implements Runnable, PicoCLIOptions {

  private static final Logger LOG = LogManager.getLogger(CommandLineTest.class);

  private final String namespace;
  private final Object options;
  private final CommandLine commandLine;
  private final String[] args;

  public CommandLineTest(final String namespace, final Object options, final String... args) {
    commandLine = new CommandLine(this);
    this.namespace = namespace;
    this.options = options;
    this.args = args;
    this.addPicoCLIOptions(namespace, options);
    this.parse();
  }

  @Override
  public void addPicoCLIOptions(final String namespace, final Object optionObject) {
    final String pluginPrefix = "--plugin-" + namespace + "-";
    final String unstablePrefix = "--Xplugin-" + namespace + "-";
    final CommandSpec mixin = CommandSpec.forAnnotatedObject(optionObject);
    boolean badOptionName = false;

    for (final CommandLine.Model.OptionSpec optionSpec : mixin.options()) {
      for (final String optionName : optionSpec.names()) {
        if (!optionName.startsWith(pluginPrefix) && !optionName.startsWith(unstablePrefix)) {
          badOptionName = true;
          LOG.error(
              "Plugin option {} did not have the expected prefix of {}", optionName, pluginPrefix);
        }
      }
    }
    if (badOptionName) {
      throw new RuntimeException("Error loading CLI options");
    } else {
      commandLine.getCommandSpec().addMixin("Plugin " + namespace, mixin);
    }
  }

  public void parse() {
    commandLine.parseWithHandlers(new CommandLine.RunLast(), new ExceptionHandler(), args);
  }

  public CommandLine getCommandLine() {
    return commandLine;
  }

  @Override
  public void run() {}

  public Object getOptions() {
    return options;
  }

  public String getNamespace() {
    return namespace;
  }
}
