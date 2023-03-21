# This is the Android Asset Packaging Tool(AAPT2).
# It is used by Gradle to package Android app resources.
# See: https://developer.android.com/studio/command-line/aapt2

{ lib, stdenv, pkgs, fetchurl }:

let
  inherit (lib) getAttr optionalString;
  inherit (stdenv) isLinux isDarwin;

  pname = "aapt2";
  # Warning: This must be the same as gradlePluginVersion android/gradle.properties
  version = "7.3.0-8691043";

  pkgPath = "com/android/tools/build/aapt2";
  repoUrl = "https://dl.google.com/dl/android/maven2";

  platform =
    if isLinux then "linux" else
    if isDarwin then "osx" else
    throw "Unknown platform!";

  filenames = {
    jar = "${pname}-${version}-${platform}.jar";
    pom = "${pname}-${version}.pom";
  };

  urls = {
    jar = fetchurl {
      url = "${repoUrl}/${pkgPath}/${version}/${filenames.jar}";
      sha256 = getAttr platform {
        linux = "sha256-AP75/vGPx4cOZ7K8w60FhZptB/836mGAdIJ2+gCtOVc=";
        osx = "sha256-Kg400fBovA3RlRpdnAPZXnPBliXFq5df+OpfhiAkvUc=";
      };
    };
    sha = fetchurl {
      url = "${repoUrl}/${pkgPath}/${version}/${filenames.jar}.sha1";
      sha256 = getAttr platform {
        linux = "sha256-908Oq40eqHlu4KtlgJQIxyUu2IOyrtErjVc+MH5JM5E=";
        osx = "sha256-hQQVMnqzaNd2v57uxWIzea21MlAuGur01T6fpcWD1fc=";
      };
    };
    pom = fetchurl {
      url = "${repoUrl}/${pkgPath}/${version}/${filenames.pom}";
      sha256 = "sha256-HVQS9Dt7vvFN/MLr7I103KTbWQw3i/eCMUL+lNDFt6Y=";
    };
  };

in stdenv.mkDerivation {
  inherit pname version;

  srcs = with urls; [ jar sha pom ];
  # patchPhase and installPhase are not neede on osx platform
  phases = ["unpackPhase" "patchPhase" "installPhase"];
  buildInputs = with pkgs; [ zip unzip patchelf ];

  unpackPhase = ''
    mkdir -p $out
    for src in $srcs; do
      filename=$(stripHash $src)
      cp $src $out/$filename
    done
    tmpDir=$(mktemp -d)
    unzip $out/${filenames.jar} -d $tmpDir
  '';

  # On Linux, we need to patch the interpreter in Java packages
  # that contain native executables to use Nix's interpreter instead.
  patchPhase = optionalString isLinux ''
    # Patch executables from maven dependency to use Nix's interpreter
    patchelf --set-interpreter "$(cat $NIX_CC/nix-support/dynamic-linker)" $tmpDir/aapt2
  '';

  # Rebuild the .jar file with patched binaries
  installPhase = optionalString isLinux ''
    pushd $tmpDir > /dev/null
    chmod u+w $out/${filenames.jar}
    zip -fr $out/${filenames.jar}
    chmod $out/${filenames.jar} --reference=$out/${filenames.jar}.sha1
    popd > /dev/null
  '';
}
