resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("uk.gov.hmrc"       % "sbt-auto-build"     % "3.6.0")
addSbtPlugin("uk.gov.hmrc"       % "sbt-distributables" % "2.1.0")
addSbtPlugin("com.typesafe.play" % "sbt-plugin"         % "2.8.15")
addSbtPlugin("org.scoverage"     % "sbt-scoverage"      % "1.9.1")
addSbtPlugin("uk.gov.hmrc"       % "sbt-service-manager"  % "0.11.0")
