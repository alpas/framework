package dev.alpas.http.jetty

import dev.alpas.*
import dev.alpas.session.SessionManager
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.util.thread.QueuedThreadPool
import java.net.BindException
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel

abstract class EngineBase {
    internal fun start(app: Application, servlet: AlpasServlet): Server {
        val sessionHandler = app.tryMake<SessionManager>()?.handler()
        val config = app.config<AppConfig>()
        return server(app, servlet, config, sessionHandler).also { it.start() }
    }

    protected open fun hostAndPort(config: AppConfig, isDev: Boolean): Pair<String?, Int> {
        val host = if (config.enableNetworkShare) null else (config.appHost ?: "localhost")
        var port = config.appPort

        if (isDev && !isTcpPortAvailable(host, port)) {
            port = getFreePort(host, port)
            println(">>> App port ${config.appPort} is not available. We'll use port '$port' instead. <<<")
        }
        return Pair(host, port)
    }

    protected open fun server(
        container: Container,
        servlet: AlpasServlet,
        config: AppConfig,
        sessionHandler: SessionHandler?
    ): Server {
        return Server(QueuedThreadPool(config.maxThreads, config.minThreads)).apply {
            val connector = connector(this, container)
            addConnector(connector)
            val requestHandler = JettyRequestHandler(sessionHandler, servlet)
            setAttribute("is-default-server", true)
            handler = HandlerCollection(true, requestHandler)
        }
    }

    protected abstract fun connector(server: Server, container: Container): ServerConnector

    private fun getFreePort(host: String?, startPort: Int): Int {
        var freePort = startPort
        if (!isTcpPortAvailable(host, freePort)) {
            do {
                freePort += 10
            } while (!isTcpPortAvailable(host, freePort))
        }
        return freePort
    }

    private fun isTcpPortAvailable(host: String?, port: Int): Boolean {
        return ServerSocketChannel.open().use {
            try {
                val bindAddress = InetSocketAddress(host ?: "0.0.0.0", port)
                it.socket().reuseAddress = true
                it.socket().bind(bindAddress)
                true
            } catch (ex: BindException) {
                false
            }
        }
    }
}
