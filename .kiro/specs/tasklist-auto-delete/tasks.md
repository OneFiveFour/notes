# Implementation Plan: TaskList AutoDelete

## Overview

The AutoDelete feature is already fully implemented across all layers (proto, domain, DTOs, mapper, repository, ViewModel, UI). The remaining work focuses on verification: writing property-based tests to formally validate the 7 correctness properties from the design, filling gaps in existing test coverage, and confirming end-to-end flag propagation through code review.

## Tasks

- [x] 1. Verify existing implementation and add missing property-based tests for mapper layer
  - [x] 1.1 Write property test for create round-trip preserving isAutoDelete (Property 3)
    - **Property 3: Mapper create round-trip preserves isAutoDelete**
    - For any valid `CreateTaskListParams`, map to `CreateTaskListRequest` via `TaskListMapper.toProto`, then construct a `TaskList` proto with the same `is_auto_delete`, and map back via `TaskListMapper.toDomain` — the resulting `TaskList.isAutoDelete` must equal the original
    - Add to `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/data/mapper/TaskListMapperPropertyTest.kt`
    - Use existing `arbCreateTaskListParams` generator and `arbProtoTaskList` generator
    - **Validates: Requirements 6.1, 3.3**

  - [x] 1.2 Write property test for update round-trip preserving isAutoDelete (Property 4)
    - **Property 4: Mapper update round-trip preserves isAutoDelete**
    - For any valid `UpdateTaskListParams`, map to `UpdateTaskListRequest` via `TaskListMapper.toProto`, then construct a `TaskList` proto with the same `is_auto_delete`, and map back via `TaskListMapper.toDomain` — the resulting `TaskList.isAutoDelete` must equal the original
    - Add to `composeApp/src/commonTest/kotlin/net/onefivefour/echolist/data/mapper/TaskListMapperPropertyTest.kt`
    - Use existing `arbUpdateTaskListParams` generator
    - **Validates: Requirements 6.2, 4.2**

- [x] 2. Checkpoint - Ensure mapper property tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 3. Add property-based tests for ViewModel layer
  - [x] 3.1 Write property test for create-mode isAutoDelete propagation (Property 1)
    - **Property 1: Create-mode isAutoDelete propagation**
    - For any boolean value of `isAutoDelete`, toggle AutoDelete to that value and save a new TaskList — the `CreateTaskListParams.isAutoDelete` passed to the repository must carry the same value
    - Add to `composeApp/src/jvmTest/kotlin/net/onefivefour/echolist/ui/edittasklist/EditTaskListViewModelPropertyTest.kt`
    - Use `Arb.boolean()` to generate random isAutoDelete values
    - **Validates: Requirements 3.1, 3.2**

  - [x] 3.2 Write property test for update-mode isAutoDelete propagation (Property 2)
    - **Property 2: Update-mode isAutoDelete propagation**
    - For any boolean value of `isAutoDelete` and any existing TaskList, set AutoDelete to that value and save — the `UpdateTaskListParams.isAutoDelete` passed to the repository must carry the same value
    - Add to `composeApp/src/jvmTest/kotlin/net/onefivefour/echolist/ui/edittasklist/EditTaskListViewModelPropertyTest.kt`
    - Use `Arb.boolean()` to generate random isAutoDelete values, reuse existing `FakeTaskListRepository`
    - **Validates: Requirements 4.1**

  - [x] 3.3 Write property test for load restoring isAutoDelete (Property 5)
    - **Property 5: Load restores isAutoDelete from backend**
    - For any `TaskList` with any `isAutoDelete` value, when the ViewModel loads it in edit mode, `EditTaskListUiState.isAutoDelete` must equal the loaded `TaskList.isAutoDelete`
    - Add to `composeApp/src/jvmTest/kotlin/net/onefivefour/echolist/ui/edittasklist/EditTaskListViewModelPropertyTest.kt`
    - Use `Arb.boolean()` to generate random isAutoDelete values
    - **Validates: Requirements 5.1, 5.2**

  - [x] 3.4 Write property test for task check/uncheck independence (Property 7)
    - **Property 7: Task check/uncheck is independent of isAutoDelete**
    - For any boolean `isAutoDelete` and any MainTask/SubTask, toggling `isDone` must update the task's `isDone` to the new value, the task must remain in the list, and the list size must be unchanged
    - Add to `composeApp/src/jvmTest/kotlin/net/onefivefour/echolist/ui/edittasklist/EditTaskListViewModelPropertyTest.kt`
    - Use `Arb.boolean()` for both `isAutoDelete` and `isDone` values
    - **Validates: Requirements 8.1, 8.2, 8.3, 8.4**

- [x] 4. Checkpoint - Ensure all ViewModel property tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 5. Verify UI layer and add unit tests for default values and edge cases
  - [x] 5.1 Verify delete icon visibility logic in MainTaskCard (Property 6)
    - **Property 6: Delete icon visibility is inverse of isAutoDelete**
    - Confirm by code inspection that `MainTaskCard` conditionally hides the delete icon with `if (!isAutoDelete)` guard
    - No automated test needed (Compose test harness not available), but add a code comment referencing Property 6
    - **Validates: Requirements 7.1, 7.2**

  - [ ] 5.2 Write unit test verifying CreateTaskListParams defaults isAutoDelete to false
    - Verify `CreateTaskListParams(name = "x", path = "y", tasks = emptyList()).isAutoDelete` is `false`
    - Add to mapper or ViewModel test file
    - _Requirements: 2.2_

  - [ ] 5.3 Write unit test verifying EditTaskListUiState defaults isAutoDelete to false in create mode
    - Verify a fresh ViewModel in create mode has `uiState.value.isAutoDelete == false`
    - Add to ViewModel test file
    - _Requirements: 2.1_

- [x] 6. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- The feature implementation is already complete — these tasks focus on verification and testing
- Property 6 (delete icon visibility) is validated by code inspection since no Compose test harness is available
- Existing tests in `EditTaskListViewModelPropertyTest.kt` and `TaskListMapperPropertyTest.kt` partially cover some properties with example-based tests; the new property-based tests use `Arb` generators for stronger guarantees
- All property tests use `PropTestConfig(iterations = 100)` per the design's testing strategy
