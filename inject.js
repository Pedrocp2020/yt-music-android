(function () {
  if (window.__ytmInjected) return;
  window.__ytmInjected = true;

  // 1) Hacer creer a la página que siempre está visible,
  //    para que no pause la música al pasar a segundo plano.
  try {
    Object.defineProperty(document, "hidden", { get: () => false, configurable: true });
    Object.defineProperty(document, "visibilityState", { get: () => "visible", configurable: true });
    document.addEventListener("visibilitychange", (e) => e.stopImmediatePropagation(), true);
  } catch (e) {}

  // 2) Ocultar banners promocionales (ej. "Prueba Premium")
  const css = document.createElement("style");
  css.textContent = `
    ytmusic-mealbar-promo-renderer,
    ytmusic-statement-banner-renderer,
    ytmusic-ad-slot-renderer,
    .ytp-ad-overlay-container { display: none !important; }
  `;
  (document.head || document.documentElement).appendChild(css);

  // 3) Saltar anuncios de audio/video: silenciar, acelerar, ir al final y pulsar "Omitir"
  let silenciado = null;
  function revisar() {
    const player = document.querySelector("#movie_player");
    const video = document.querySelector("video");
    if (!player || !video) return;

    const hayAnuncio =
      player.classList.contains("ad-showing") ||
      player.classList.contains("ad-interrupting");

    if (hayAnuncio) {
      if (silenciado === null) silenciado = video.muted;
      video.muted = true;
      video.playbackRate = 16;
      if (isFinite(video.duration) && video.duration > 0) {
        video.currentTime = video.duration;
      }
      document
        .querySelectorAll(
          ".ytp-skip-ad-button, .ytp-ad-skip-button, .ytp-ad-skip-button-modern, .ytp-ad-skip-button-container button"
        )
        .forEach((b) => b.click());
    } else if (silenciado !== null) {
      video.muted = silenciado;
      video.playbackRate = 1;
      silenciado = null;
    }
  }
  setInterval(revisar, 300);
})();
