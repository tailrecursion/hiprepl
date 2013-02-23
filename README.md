# hiprepl

<img src="http://tailrecursion.com/~alan/pix/hiprepl.png" alt="hiprepl usage"/>

## Usage

* Install [Leiningen](https://github.com/technomancy/leiningen)
* Install a `~/.java.policy` like [this one](https://raw.github.com/flatland/clojail/master/example.policy)
* `lein run <api-token> <room-name>`
* Evaluate Clojure expressions in the room by prefixing them with a comma, like: `,(+ 1 2)`

## Notes

HipChat API requests are [rate
limited](https://www.hipchat.com/docs/api/rate_limiting), and various
functions are memoized to prevent hitting it.  As a result, you may
need to restart the bot if you add rooms or users.