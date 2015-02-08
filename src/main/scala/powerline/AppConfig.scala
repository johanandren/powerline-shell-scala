package powerline

case class AppConfig(debug: Boolean, shell: PromptGenerator, serverPort: Int = 18888)