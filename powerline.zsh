function _powerline_ps1() {
  export PS1="$(target/powerline-client zsh $?)"
}
export PROMPT_COMMAND="_powerline_ps1"
