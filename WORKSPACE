workspace(name = "sync_index")
load("//:bazlets.bzl", "load_bazlets")

load_bazlets(
    commit = "0f87babe07a555425d829c6e7951e296e9e24579",
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
    name = "wiremock",
    artifact = "com.github.tomakehurst:wiremock-standalone:2.4.1",
    sha1 = "228d3047147fffa0f12771f5dc2b3cd3b38c454b",
)

maven_jar(
    name = 'mockito',
    artifact = 'org.mockito:mockito-core:2.5.0',
    sha1 = 'be28d46a52c7f2563580adeca350145e9ce916f8',
)

maven_jar(
    name = 'byte-buddy',
    artifact = 'net.bytebuddy:byte-buddy:1.5.12',
    sha1 = 'b1ba1d15f102b36ed43b826488114678d6d413da',
)

maven_jar(
    name = 'objenesis',
    artifact = 'org.objenesis:objenesis:2.4',
    sha1 = '2916b6c96b50c5b3ec4452ed99401db745aabb27',
)
