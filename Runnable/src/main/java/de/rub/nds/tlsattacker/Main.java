/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS.
 *
 * Copyright (C) 2015 Chair for Network and Data Security,
 *                    Ruhr University Bochum
 *                    (juraj.somorovsky@rub.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.rub.nds.tlsattacker;

import com.beust.jcommander.JCommander;
import de.rub.nds.tlsattacker.attacks.config.BleichenbacherCommandConfig;
import de.rub.nds.tlsattacker.attacks.config.DtlsPaddingOracleAttackCommandConfig;
import de.rub.nds.tlsattacker.attacks.config.InvalidCurveAttackCommandConfig;
import de.rub.nds.tlsattacker.attacks.config.InvalidCurveAttackFullCommandConfig;
import de.rub.nds.tlsattacker.attacks.config.HeartbleedCommandConfig;
import de.rub.nds.tlsattacker.attacks.config.PaddingOracleCommandConfig;
import de.rub.nds.tlsattacker.attacks.config.PoodleCommandConfig;
import de.rub.nds.tlsattacker.attacks.config.WinshockCommandConfig;
import de.rub.nds.tlsattacker.attacks.impl.BleichenbacherAttack;
import de.rub.nds.tlsattacker.attacks.impl.DtlsPaddingOracleAttack;
import de.rub.nds.tlsattacker.attacks.impl.InvalidCurveAttack;
import de.rub.nds.tlsattacker.attacks.impl.InvalidCurveAttackFull;
import de.rub.nds.tlsattacker.attacks.impl.HeartbleedAttack;
import de.rub.nds.tlsattacker.attacks.impl.PaddingOracleAttack;
import de.rub.nds.tlsattacker.attacks.impl.PoodleAttack;
import de.rub.nds.tlsattacker.attacks.impl.WinshockAttack;
import de.rub.nds.tlsattacker.fuzzer.config.MultiFuzzerConfig;
import de.rub.nds.tlsattacker.fuzzer.impl.MultiFuzzer;
import de.rub.nds.tlsattacker.tls.Attacker;
import de.rub.nds.tlsattacker.tls.config.ClientCommandConfig;
import de.rub.nds.tlsattacker.tls.config.CommandConfig;
import de.rub.nds.tlsattacker.tls.config.ConfigHandler;
import de.rub.nds.tlsattacker.tls.config.ConfigHandlerFactory;
import de.rub.nds.tlsattacker.tls.config.GeneralConfig;
import de.rub.nds.tlsattacker.tls.config.WorkflowTraceSerializer;
import de.rub.nds.tlsattacker.tls.exceptions.ConfigurationException;
import de.rub.nds.tlsattacker.tls.exceptions.WorkflowExecutionException;
import de.rub.nds.tlsattacker.tls.util.LogLevel;
import de.rub.nds.tlsattacker.tls.workflow.TlsContext;
import de.rub.nds.tlsattacker.tls.workflow.WorkflowExecutor;
import de.rub.nds.tlsattacker.transport.TransportHandler;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author Juraj Somorovsky <juraj.somorovsky@rub.de>
 */
public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

	GeneralConfig generalConfig = new GeneralConfig();
	JCommander jc = new JCommander(generalConfig);

	MultiFuzzerConfig cmconfig = new MultiFuzzerConfig();
	jc.addCommand(MultiFuzzerConfig.ATTACK_COMMAND, cmconfig);

	BleichenbacherCommandConfig bleichenbacherTest = new BleichenbacherCommandConfig();
	jc.addCommand(BleichenbacherCommandConfig.ATTACK_COMMAND, bleichenbacherTest);
	DtlsPaddingOracleAttackCommandConfig dtlsPaddingOracleAttackTest = new DtlsPaddingOracleAttackCommandConfig();
	jc.addCommand(DtlsPaddingOracleAttackCommandConfig.ATTACK_COMMAND, dtlsPaddingOracleAttackTest);
	InvalidCurveAttackCommandConfig ellipticTest = new InvalidCurveAttackCommandConfig();
	jc.addCommand(InvalidCurveAttackCommandConfig.ATTACK_COMMAND, ellipticTest);
	InvalidCurveAttackFullCommandConfig elliptic = new InvalidCurveAttackFullCommandConfig();
	jc.addCommand(InvalidCurveAttackFullCommandConfig.ATTACK_COMMAND, elliptic);
	HeartbleedCommandConfig heartbleed = new HeartbleedCommandConfig();
	jc.addCommand(HeartbleedCommandConfig.ATTACK_COMMAND, heartbleed);
	PaddingOracleCommandConfig paddingOracle = new PaddingOracleCommandConfig();
	jc.addCommand(PaddingOracleCommandConfig.ATTACK_COMMAND, paddingOracle);
	PoodleCommandConfig poodle = new PoodleCommandConfig();
	jc.addCommand(PoodleCommandConfig.ATTACK_COMMAND, poodle);
	WinshockCommandConfig winshock = new WinshockCommandConfig();
	jc.addCommand(WinshockCommandConfig.ATTACK_COMMAND, winshock);
	// ServerCommandConfig server = new ServerCommandConfig();
	// jc.addCommand(ServerCommandConfig.COMMAND, server);
	ClientCommandConfig client = new ClientCommandConfig();
	jc.addCommand(ClientCommandConfig.COMMAND, client);

	jc.parse(args);

	if (generalConfig.isHelp() || jc.getParsedCommand() == null) {
	    jc.usage();
	    return;
	}

	Attacker attacker;
	switch (jc.getParsedCommand()) {
	    case MultiFuzzerConfig.ATTACK_COMMAND:
		startMultiFuzzer(cmconfig, generalConfig, jc);
		return;
		// case ServerCommandConfig.COMMAND:
		// startSimpleTls(generalConfig, server, jc);
		// return;
	    case ClientCommandConfig.COMMAND:
		startSimpleTls(generalConfig, client, jc);
		return;
	    case BleichenbacherCommandConfig.ATTACK_COMMAND:
		attacker = new BleichenbacherAttack(bleichenbacherTest);
		break;
	    case InvalidCurveAttackCommandConfig.ATTACK_COMMAND:
		attacker = new InvalidCurveAttack(ellipticTest);
		break;
	    case InvalidCurveAttackFullCommandConfig.ATTACK_COMMAND:
		attacker = new InvalidCurveAttackFull(elliptic);
		break;
	    case HeartbleedCommandConfig.ATTACK_COMMAND:
		attacker = new HeartbleedAttack(heartbleed);
		break;
	    case PoodleCommandConfig.ATTACK_COMMAND:
		attacker = new PoodleAttack(poodle);
		break;
	    case PaddingOracleCommandConfig.ATTACK_COMMAND:
		attacker = new PaddingOracleAttack(paddingOracle);
		break;
	    case WinshockCommandConfig.ATTACK_COMMAND:
		attacker = new WinshockAttack(winshock);
		break;
	    case DtlsPaddingOracleAttackCommandConfig.ATTACK_COMMAND:
		attacker = new DtlsPaddingOracleAttack(dtlsPaddingOracleAttackTest);
		break;
	    default:
		throw new ConfigurationException("No command found");
	}
	ConfigHandler configHandler = ConfigHandlerFactory.createConfigHandler("client");
	configHandler.initialize(generalConfig);

	if (configHandler.printHelpForCommand(jc, attacker.getConfig())) {
	    return;
	}

	attacker.executeAttack(configHandler);

	CommandConfig config = attacker.getConfig();
	if (config.getWorkflowTraceOutputFile() != null && !config.getWorkflowTraceOutputFile().isEmpty()) {
	    logWorkflowTraces(attacker.getTlsContexts(), config.getWorkflowTraceOutputFile());
	}
    }

    private static void startMultiFuzzer(MultiFuzzerConfig fuzzerConfig, GeneralConfig generalConfig, JCommander jc) {
	MultiFuzzer fuzzer = new MultiFuzzer(fuzzerConfig, generalConfig);
	if (fuzzerConfig.isHelp()) {
	    jc.usage(MultiFuzzerConfig.ATTACK_COMMAND);
	    return;
	}
	fuzzer.startFuzzer();
    }

    private static void startSimpleTls(GeneralConfig generalConfig, CommandConfig config, JCommander jc)
	    throws JAXBException, IOException {
	ConfigHandler configHandler = ConfigHandlerFactory.createConfigHandler(jc.getParsedCommand());
	configHandler.initialize(generalConfig);

	if (configHandler.printHelpForCommand(jc, config)) {
	    return;
	}

	TransportHandler transportHandler = configHandler.initializeTransportHandler(config);
	TlsContext tlsContext = configHandler.initializeTlsContext(config);
	WorkflowExecutor workflowExecutor = configHandler.initializeWorkflowExecutor(transportHandler, tlsContext);

	try {
	    workflowExecutor.executeWorkflow();
	} catch (WorkflowExecutionException ex) {
	    LOGGER.info(ex.getLocalizedMessage(), ex);
	    LOGGER.log(LogLevel.CONSOLE_OUTPUT,
		    "The TLS protocol flow was not executed completely, follow the debug messages for more information.");
	}

	transportHandler.closeConnection();

	if (config.getWorkflowTraceOutputFile() != null && !config.getWorkflowTraceOutputFile().isEmpty()) {
	    FileOutputStream fos = new FileOutputStream(config.getWorkflowTraceOutputFile());
	    WorkflowTraceSerializer.write(fos, tlsContext.getWorkflowTrace());
	}
    }

    private static void logWorkflowTraces(List<TlsContext> tlsContexts, String fileName) throws JAXBException,
	    FileNotFoundException, IOException {
	int i = 0;
	for (TlsContext context : tlsContexts) {
	    i++;
	    FileOutputStream fos = new FileOutputStream(fileName + i);
	    WorkflowTraceSerializer.write(fos, context.getWorkflowTrace());
	}
    }
}
