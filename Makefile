GRADLE := ./gradlew
VERSION_FILE := version.properties
DIST := dist

# Pass increase-version=false to build a release without bumping the version.
increase-version ?= true

.PHONY: help debug release install test clean version bump-version

help:
	@echo "make release                        Bump version, build release APK + AAB into $(DIST)/, then commit + git-tag the new version"
	@echo "make release increase-version=false  Build release keeping the latest committed version (no bump, no commit/tag)"
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
	bumped=0; \
	case "$(increase-version)" in \
		false|no|0) echo "Keeping version: $$(sed -n 's/^VERSION_NAME=//p' $(VERSION_FILE))" ;; \
		*) $(MAKE) --no-print-directory bump-version; bumped=1 ;; \
	esac; \
	if ! $(GRADLE) assembleRelease bundleRelease; then \
		mv $(VERSION_FILE).bak $(VERSION_FILE); \
		echo "Build failed; version restored."; \
		exit 1; \
	fi; \
	rm -f $(VERSION_FILE).bak; \
	mkdir -p $(DIST); \
	cp app/build/outputs/apk/release/*.apk app/build/outputs/bundle/release/*.aab $(DIST)/; \
	echo "Release artifacts:"; ls -1 $(DIST)/*release*; \
	if [ "$$bumped" = "1" ]; then \
		name=$$(sed -n 's/^VERSION_NAME=//p' $(VERSION_FILE)); \
		if git rev-parse --git-dir >/dev/null 2>&1; then \
			git add $(VERSION_FILE); \
			git commit -m "Release v$$name" >/dev/null && echo "Committed version bump: v$$name"; \
			if git rev-parse "v$$name" >/dev/null 2>&1; then \
				echo "Tag v$$name already exists; skipping tag."; \
			else \
				git tag "v$$name" && echo "Tagged: v$$name"; \
			fi; \
		else \
			echo "Not a git repository; skipping commit/tag."; \
		fi; \
	fi

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
