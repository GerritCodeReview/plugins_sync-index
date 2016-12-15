load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "gerrit_plugin",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
)

gerrit_plugin(
    name = "sync-index",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-PluginName: sync-index",
        "Gerrit-Module: com.ericsson.gerrit.plugins.syncindex.Module",
        "Gerrit-HttpModule: com.ericsson.gerrit.plugins.syncindex.HttpModule",
        "Implementation-Title: sync-index plugin",
        "Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/sync-index",
        "Implementation-Vendor: Ericsson",
    ],
    resources = glob(["src/main/**/*"]),
)

junit_tests(
    name = "sync_index_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = [
        "local",
        "sync-index",
    ],
    deps = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        "@wiremock//jar",
        ":sync-index__plugin",
    ],
)
