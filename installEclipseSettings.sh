# Formatter settings
JDT_CORE_PREFS="src/main/eclipse/org.eclipse.jdt.core.prefs"

# Save actions
JDT_UI_PREFS="src/main/eclipse/org.eclipse.jdt.ui.prefs"

function installPrefs {
  mkdir -p $1/.settings/
  cp -v $JDT_CORE_PREFS $1/.settings/
  cp -v $JDT_UI_PREFS $1/.settings/
}

installPrefs cleartk-bom
installPrefs cleartk-berkeleyparser
installPrefs cleartk-clearnlp
installPrefs cleartk-corpus
installPrefs cleartk-eval
installPrefs cleartk-examples
installPrefs cleartk-feature
installPrefs cleartk-maltparser
installPrefs cleartk-ml
installPrefs cleartk-ml-crfsuite
installPrefs cleartk-ml-liblinear
installPrefs cleartk-ml-libsvm
installPrefs cleartk-ml-libsvm-tk
installPrefs cleartk-ml-mallet
installPrefs cleartk-ml-opennlp-maxent
installPrefs cleartk-ml-script
installPrefs cleartk-ml-svmlight
installPrefs cleartk-ml-tksvmlight
installPrefs cleartk-ml-weka
installPrefs cleartk-opennlp-tools
installPrefs cleartk-snowball
installPrefs cleartk-stanford-corenlp
installPrefs cleartk-summarization
installPrefs cleartk-test-util
installPrefs cleartk-timeml
installPrefs cleartk-token
installPrefs cleartk-type-system
installPrefs cleartk-util
