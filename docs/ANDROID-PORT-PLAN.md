# G.I KO KO Claude Agent Studio — Android Port Plan

## Repository assessment

The upstream project is a Node.js 20.12+ / TypeScript application (v0.6.4), not an Android application. Its current deliverables are a CLI, an MCP server, and a browser-based local Studio. The existing engine depends on Playwright/Chromium, FFmpeg, and native `sharp` processing. The current CI builds and tests the Node application on Ubuntu and Windows; the release workflow publishes an npm package. There is no Android Gradle project or APK workflow in the repository.

A direct WebView-only APK or a simple ABI rename would not make the existing Node/Playwright engine run on Android. In particular, Chromium automation and native media dependencies need a separate Android-compatible execution path.

## Product target

- App name: **G.I KO KO CLAUDE AGENT STUDIO**
- UI: Claude-inspired mobile chat layout, adapted for compact screens.
- Device target: Redmi 9A, Android 10 (API 29), ARMv7 32-bit (`armeabi-v7a`).
- Build: GitHub Actions; no local JDK/Android Studio requirement.
- Preserve the upstream Node CLI/MCP/Studio and all existing features.

## Proposed architecture

### 1. Android client (new `android/` Gradle module)

Native Kotlin Android app with:
- Chat interface, streaming responses, conversation history, project workspaces.
- Provider settings for Anthropic-compatible and OpenAI-compatible APIs, including custom base URL, API key, model discovery, free-model filtering where provider metadata permits, and connection test.
- Secure API-key storage using Android Keystore-backed encryption.
- File picker and scoped-storage-compatible import/export.
- Skills browser/editor for local `SKILL.md` files, enable/disable controls, and import/export.
- Agent task timeline with Plan / Running / Review / Completed states.
- Explicit approval gates for file writes, deletes, terminal commands, and publishing.
- Accessibility and low-memory UI behavior suitable for 3–4 GB RAM devices.

### 2. Agent execution adapters

Keep the existing TypeScript engine as the source of truth. Add adapters rather than copying or deleting upstream functionality:
- **GitHub adapter:** inspect repositories, create feature branches, edit files, review diffs, and monitor GitHub Actions.
- **Local Termux bridge (optional):** connect to a user-started local Node service on loopback for compatible CLI/MCP tasks. The Android app must clearly show when this bridge is disconnected.
- **Remote MCP/agent endpoint (optional):** connect only to endpoints explicitly configured by the user.
- Do not claim that the Android app itself embeds Claude Code or grants a Claude subscription. Model access and agent execution depend on the configured provider/backend.

### 3. Media-engine compatibility

The upstream browser recording and FFmpeg pipeline remains intact for desktop/Node environments. Android support must be feature-gated:
- Use Android-native media APIs or a separately maintained ARMv7-compatible media adapter for on-device operations.
- Route unsupported Playwright/Chromium automation to a compatible Termux or remote backend, with a clear capability status.
- Never label unsupported desktop features as available on the phone.

## Build and verification

- Add a dedicated GitHub Actions workflow for the Android debug/release APK.
- Set `minSdk 29`; compile against a current Android SDK; include `armeabi-v7a` and verify the produced APK actually contains 32-bit native libraries where required.
- Run Gradle unit tests and APK packaging in GitHub Actions.
- Inspect the APK manifest, package ID, min SDK, ABI contents, and artifact before calling the build successful.
- Keep the existing Node CI and npm release workflows unchanged.

## Delivery phases

1. **Foundation:** Android Gradle project, app shell, Claude-inspired navigation/chat UI, ARMv7 GitHub Actions workflow.
2. **Core app:** provider management, secure key storage, streaming chat, history, file import/export.
3. **Agent tools:** project browser, GitHub operations, diff review, approval gates, Skills Manager.
4. **MCP and execution:** configured MCP connections and optional Termux/remote execution bridge.
5. **Compatibility and release:** Redmi 9A smoke testing, memory/performance tuning, signed release configuration, verified APK artifact.

## Acceptance criteria

- Existing Node tests and workflows remain green.
- Android APK installs on Android 10 and is built for ARMv7 (not ARM64-only).
- Core chat works with a user-configured compatible provider.
- API keys are not committed to the repository or written to plaintext logs.
- Skills and agent operations expose clear permission/approval states.
- Unsupported features are reported honestly instead of silently failing.
