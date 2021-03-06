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
package de.rub.nds.tlsattacker.tls.workflow;

import de.rub.nds.tlsattacker.dtls.workflow.Dtls12WorkflowExecutor;
import de.rub.nds.tlsattacker.transport.TransportHandler;

/**
 * @author Juraj Somorovsky <juraj.somorovsky@rub.de>
 */
public class WorkflowExecutorFactory {

    public static WorkflowExecutor createWorkflowExecutor(TransportHandler transportHandler, TlsContext tlsContext) {
	WorkflowExecutor we;
	switch (tlsContext.getProtocolVersion()) {
	    case TLS10:
	    case TLS11:
	    case TLS12:
		we = new GenericWorkflowExecutor(transportHandler, tlsContext);
		return we;
	    case DTLS12:
		we = new Dtls12WorkflowExecutor(transportHandler, tlsContext);
		return we;
	    default:
		throw new UnsupportedOperationException("not yet implemented");
	}
    }
}
