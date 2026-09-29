GRADLE := ./gradlew
VERSION_FILE := version.properties
DIST := dist

# Pass increase-version=false to build a release without bumping the version.
increase-version ?= true

.PHONY: help debug release install test clean version bump-version

help:
	@echo "make release                        Bump version, build release APK + AAB into $(DIST)/"
	@echo "make release increase-version=false  Build release without bumping the version"
	@echo "make debug                          Build debug APK into $(DIST)/"
	@echo "make install                        Install debug build on a connected device"
	@echo "make test                           Run unit tests"
	@echo "make version                        Show current version"
	@echo "make bump-version                   Bump version without building"
	@echo "make clean                          Remove build outputs"

version:
	@cat $(VERSION_FILE)

bump-version:
	@code=$$(sed -n 's/^VERSION_CODE=//p' $(VERSION_FILE)); \
	name=$$(sed -n 's/^VERSION_NAME=//p' $(VERSION_FILE)); \
	new_code=$$((code + 1)); \
	new_name=$$(echo "$$name" | awk -F. -v OFS=. '{ $$NF = $$NF + 1; print }'); \
	printf 'VERSION_CODE=%s\nVERSION_NAME=%s\n' "$$new_code" "$$new_name" > $(VERSION_FILE); \
	echo "Version: $$name ($$code) -> $$new_name ($$new_code)"

release:
	@set -e; \
	[ -f keystore.properties ] || echo "WARNING: keystore.properties not found; release build will be unsigned."; \
	cp $(VERSION_FILE) $(VERSION_FILE).bak; \
	case "$(increase-version)" in \
		false|no|0) echo "Keeping version: $$(sed -n 's/^VERSION_NAME=//p' $(VERSION_FILE))" ;; \
		*) $(MAKE) --no-print-directory bump-version ;; \
	esac; \
	if ! $(GRADLE) assembleRelease bundleRelease; then \
		mv $(VERSION_FILE).bak $(VERSION_FILE); \
		echo "Build failed; version restored."; \
		exit 1; \
	fi; \
	rm -f $(VERSION_FILE).bak; \
	mkdir -p $(DIST); \
	cp app/build/outputs/apk/release/*.apk app/build/outputs/bundle/release/*.aab $(DIST)/; \
	echo "Release artifacts:"; ls -1 $(DIST)/*release*

debug:
	$(GRADLE) assembleDebug
	@mkdir -p $(DIST)
	@cp app/build/outputs/apk/debug/*.apk $(DIST)/
	@ls -1 $(DIST)/*debug*.apk

install:
	$(GRADLE) installDebug

test:
	$(GRADLE) testDebugUnitTest

clean:
	$(GRADLE) clean
	rm -rf $(DIST)
