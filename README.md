# Camera Filter Engine

Camera Filter Engine은 CameraX 프리뷰 스트림을 OpenGL ES 렌더링 파이프라인에 연결하고, 실시간으로 필터를 적용하는 Android 과제 프로젝트입니다.

# 0. 구현 기술

## 0-1. 기술 목록
- 카메라 스택: `CameraX` 사용
- Filter Chain:
  - Chain 구조 선택
- Filter Implementation:
  - OpenGL ES 셰이더로 `Grayscale` 필터 구현
- Architecture
  - 단일 모듈 내 모듈 분리 + Clean Architecture + MVI
- Frame 처리 속도 UI
  - 우측 상단 표시 (ms)
- 동시성 제어
  - GLSurfaceView 사용하여 별도 GL 스레드 생성
  - 프레임 처리 분리
    - @Volatile isFrameAvailable 플래그로 신호만 전달, 실제 처리는 GL 스레드에서 진행
  - @Volatile pendingFilters를 사용하여 UI 스레드의 필터 변경을 GL 스레드에 전달

## 1. 기술 선택 이유

### 1-1. Architecture

단일 모듈 내 모듈 분리 + Clean Architecture + MVI을 선택했습니다.

선택 이유:
- 단일 모듈 내 모듈 분리
  - 단일 모듈 선택
    - 개발 진행시 Gradle 설정, 의존성 관리 등 복잡도가 증가하는데, 멀티 모듈 선택은 과제 규모에 비해 개발 비용이 크다 판단했습니다.
  - 모듈 내 모듈 분리
    - 확장 가능성을 고려해 구조 자체는 모듈화 할 수 있도록 구분하여 설계했습니다.
- Clean Architecture 
  - 핵심 비즈니스 로직을 UI와 분리해 확장 가능하도록 하기 위해 Clean Architecture 를 선택했습니다.
- MVI
  - 필터 적용 및 Performance Monitoring 기능 등의 상태 변경을 단방향으로 제한하는 것이 디버깅 및 상태 흐름 파악에 좋을 것으로 판단해 선택했습니다.
  - 권한 요청 같은 일회성 이벤트는 Effect 로 분리하면 안전하게 처리할 수 있어 MVI를 선택했습니다.

### 1-2. 카메라 스택

- CameraX / Camera2

선택 이유:
- Camera 기능 단순 구현이 아닌 Filter Implementation 개발이기 때문에 구현 난이도가 낮은 CameraX를 선택했습니다.
- Camera2는 카메라 기능을 세밀하게 제어할 수 있는 저수준 API 이지만, 보다 복잡하고 다루기 어렵다는 비교 자료가 있어, 처음 접하는 상황에서는 빠른 개발이 가능한 CameraX 를 선택했습니다.

<details><summary><strong>Link</strong></summary>
- https://developer.android.com/media/camera/choose-camera-library?hl=ko
- https://medium.com/@seungbae2/camera2-vs-camerax-a-comparison-of-android-camera-apis-5db2b5ff302e
- https://audwns419.tistory.com/72
</details>

### 1-3. 왜 OpenGL ES를 선택했는가

- 방법 A: OpenGL ES 기반 Grayscale 셰이더 처리
- 방법 B: YUV / Buffer 데이터를 직접 가공하는 방식의 처리

이 프로젝트에서는 방법 A인 OpenGL ES를 선택했습니다.

선택 이유:
- 방법 A는 초기 학습 곡선이 가파르지만, Grayscale 셰이더 구현 자체는 비교적 단순해 과제 범위 안에서 완성 가능하다고 판단 
- 방법 B는 픽셀 단위 연산을 CPU에서 수행하므로 초기 학습 곡선에 비해 과제 요구 사항에 도달하는 과정이 오래 걸릴 것으로 판단

> 방법 A, B 둘다 처음 접하는 기술로 B로 과제를 진행할 시 학습에 실패 했을 경우의 리스크가 더 크다고 판단했습니다.
> 
> A의 경우 초기 학습 과정에 관련 듀토리얼 등이 많아 학습에 실패 했을 경우 과제 수행을 완수 할 수 있을 것으로 판단해서 OpenGL ES 기반 GrayScale 셰이더 처리를 선택했습니다.

<details><summary><strong>Link</strong></summary>
- https://www.reddit.com/r/opengl/comments/2gh77v/help_am_i_so_stupid_or_its_really_hard_to/?utm_source=chatgpt.com
</details>

### 1-4. Filter Chain 설계

- Pipeline 구조 / Chain 구조

선택 이유 :
- Pipeline은 각 단계가 다음 단계를 직접 참조해야 하지만, Chain 구조는 각 필터가 다음 단계를 몰라도 되기 때문에 결합도가 낮음
- 단순 고정 흐름이라면 Pipeline으로 개발하는 것이 쉽지만 확장성을 고려하여 결합도가 낮은 Chain이 적합하다 생각함

> Pipeline/Chain 구조 선택을 고민하는 중 Retrica의 내부에 많은 프리셋을 추가/제거할 수 있는 구조가 떠올랐고, 많은 프리셋을 추가 제거하기 위해서 복잡한 구조를 가지고 있고, 자칫 결합이 높아 유지보수 비용이 높아질 수도 있는 Pipeline 보다 결합도가 낮고, 단순화 할 수 있는 Chain 패턴이 확장성이 높다 판단했습니다.

## 2. 동시성 제어

과제 요구사항 중 하나는 카메라 프레임 드랍이 UI 스레드에 영향을 주지 않도록 구조를 설계하는 것입니다.

이 프로젝트에서는 다음 방식으로 대응했습니다.

- 카메라 프레임 수신은 CameraX와 `SurfaceTexture` 기반 비동기 콜백 사용
- 화면 상태 관리와 렌더링 처리를 분리
- UI는 Compose 상태만 관찰하고, 실제 프레임 처리는 GL 렌더러에서 수행
- 필터 변경은 렌더 스레드에서 반영되도록 `pendingFilters`를 사용

즉, 버튼 클릭과 권한 처리 같은 UI 이벤트와 실제 프레임 가공 루프를 분리해 UI 지연 가능성을 줄였습니다.

## 3. 데이터 흐름

1. 카메라 권한 상태 확인
2. 권한 허용 시 `CameraPreview`가 `CameraRenderer` 생성
3. `CameraRenderer`는 onSurfaceCreated() 초기화 진행
4. bindCamera()를 통해 CameraX `Preview`와 연결
5. CameraX `Preview`가 `SurfaceTexture`에 프레임 전달
6. 프레임 도착 시 렌더 루프에서 프레임을 최신 Texture로 업데이트
7. `GlFilterChain`에서 원본 또는 필터 체인을 처리
8. 프레임 처리 속도는 ms 로 UI에 오버레이되어 표시

## 4. 성능 최적화 전략

### 4-1. 필터 체인 재구성
- 개선 전: 매 프레임마다 `rebuildFilterChain()`이 호출되어 셰이더가 불필요하게 재생성됨
- 개선 후: `pendingFilters`가 null이 아닐 때만 재구성하게 함

### 4-2. 프레임 처리 시간이 높음 (미반영)
- 현재 상황
  - 프레임 처리 시간이 12~18ms로 측정되어 간헐적인 프레임 드랍이 관측됨
- 원인 분석
  - `RENDERMODE_CONTINUOUSLY`로 설정되어 있어 매 루프마다 GL 렌더링이 실행됨 
  - `isFrameAvailable` 플래그와 무관하게 `onDrawFrame()`이 계속 호출 됨
- 해결 방향 (미반영)
  - `RENDERMODE_WHEN_DIRTY`로 변경
  - `setOnFrameAvailableListener`에서 새 프레임 도착 시에만 request
