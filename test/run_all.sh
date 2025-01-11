SCRIPTPATH="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

async_dependency="{org.clojure/core.async {:mvn/version \"1.6.673\"}}"

DEPS="{:deps ${async_dependency}}"

# Correct order of tests
#clj -Sdeps "${DEPS}" -M $SCRIPTPATH/channel_define.clj
#clj -Sdeps "${DEPS}" -M $SCRIPTPATH/channel_create.clj
#clj -Sdeps "${DEPS}" -M $SCRIPTPATH/node_define.clj
#clj -Sdeps "${DEPS}" -M $SCRIPTPATH/node_create.clj
clj -Sdeps "${DEPS}" -M $SCRIPTPATH/conveyor_create.clj
