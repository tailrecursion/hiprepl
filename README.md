# hiprepl

<img src="http://tailrecursion.com/~alan/pix/hiprepl_xmpp.png" alt="hiprepl usage"/>

## Usage

* Install [Leiningen](https://github.com/technomancy/leiningen).
* Install a `~/.java.policy` like [this one](https://raw.github.com/flatland/clojail/master/example.policy).
* Create a HipChat user for your bot.
* [Log in](https://www.hipchat.com/sign_in) as the bot user and visit [this page](https://hipchat.com/account/xmpp).
* Copy `example_config.clj` to `config.clj` and modify it using details from that page.  All keys are required.
* `lein run config.clj`
* Evaluate Clojure expressions in the room by prefixing them with a comma, like: `,(+ 1 2)`

## Thanks

Big ups to [Zach Kim](http://zacharykim.com/) for his
[xmpp-clj](https://github.com/zkim/xmpp-clj) project, on which much of
this code is based.