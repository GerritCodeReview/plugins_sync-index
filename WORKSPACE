workspace(name = "sync_index")
load("//:bazlets.bzl", "load_bazlets")

load_bazlets(
    commit = "8a4cbdc993f41fcfe7290e7d1007cfedf8d87c18",
    #local_path = "/home/davido/projects/bazlets",
)

#Snapshot Plugin API
load(
    "@com_googlesource_gerrit_bazlets//:gerrit_api_maven_local.bzl",
    "gerrit_api_maven_local",
)

# Load snapshot Plugin API
gerrit_api_maven_local()

# Release Plugin API
#load(
#   "@com_googlesource_gerrit_bazlets//:gerrit_api.bzl",
#   "gerrit_api",
#)

# Load release Plugin API
#gerrit_api()

load("@com_googlesource_gerrit_bazlets//tools:maven_jar.bzl", "maven_jar")

maven_jar(
  name = 'wiremock',
  artifact = 'com.github.tomakehurst:wiremock-standalone:2.1.12',
  sha1 = 'fee73e0913ff3801cf83e660a14d6e4aac9a9cbd',
)
