package potato.common.exception

class PotatoException(msg: String = null, throwable: Throwable = null) extends Exception(msg, throwable)

case class ConfigException(msg: String = null, throwable: Throwable = null) extends PotatoException(msg, throwable)

case class ConfigNotFoundException(msg: String = null, throwable: Throwable = null) extends PotatoException(msg, throwable)

case class ArgParseException(msg: String = null, throwable: Throwable = null) extends PotatoException(msg, throwable)

case class MethodNotAllowedException(msg: String = null, throwable: Throwable = null) extends PotatoException(msg, throwable)
