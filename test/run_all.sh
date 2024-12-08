SCRIPTPATH="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

# Correct order of tests
clj -M $SCRIPTPATH/channel_define.clj
clj -M $SCRIPTPATH/channel_create.clj
clj -M $SCRIPTPATH/node_define.clj
clj -M $SCRIPTPATH/node_create.clj