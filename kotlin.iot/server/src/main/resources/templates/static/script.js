async function initPage() {
    sseSubscribe().then();
    ssePushNotifications().then();
}

async function sseSubscribe() {
    let eventElement = document.getElementById("event_id");
    let eventSource = new EventSource('/subscribe');
    eventSource.addEventListener('state', e => {
        const state = JSON.parse(e.data);
        let newElement = "<div class=\"state\"><a><i>" + new Date().toLocaleTimeString() + ":</i> ";
        let addComma = false;
        if (state.temperature != null) {
            newElement = newElement + "temperature: " + state.temperature + "℃";
            addComma = true;
        }
        if (state.waterLevel != null) {
            if (addComma) {
                newElement = newElement + " , ";
            } else {
                addComma = true;
            }
            newElement = newElement + "water level: " + state.waterLevel + "%";
        }
        if (state.pirIsActive != null) {
            if (addComma) {
                newElement = newElement + " , ";
            } else {
                addComma = true;
            }
            newElement = newElement + "presence: " + state.pirIsActive;
        }
        if (state.nextTemperature != null) {
            if (addComma) {
                newElement = newElement + " , ";
            }
            newElement = newElement + "next checkpoint: " + state.nextTemperature + "℃";
        }
        eventElement.insertAdjacentHTML('beforeend', newElement);
    });
    eventSource.addEventListener('error', e => {
        const error = JSON.parse(e.data);
        let newElement = "<div class=\"errorState\"><a><i>" + new Date().toLocaleTimeString() + ":</i> error: " + error.error;
        eventElement.insertAdjacentHTML('beforeend', newElement);
    });
}

async function ssePushNotifications() {
    let pushElement = document.getElementById("push_id");
    let pushTime = document.getElementById("push_time");
    let eventSource = new EventSource('/push');
    eventSource.addEventListener('push', e => {
        if (pushElement == null) {
            let topElement = document.getElementById("top_page");
            topElement.insertAdjacentHTML('afterend', "\n" +
                "<div class=\"push\" id=\"push_bubble\">\n" +
                "<p id=\"push_time\"></p>\n" +
                "<p id=\"push_id\"></p>\n" +
                "</div>\n");
            pushElement = document.getElementById("push_id");
            pushTime = document.getElementById("push_time");
        }
        pushTime.textContent = new Date().toLocaleTimeString()
        pushElement.textContent = e.data;
    });
}

async function sendConfig(e) {
    e.preventDefault();
    let dataText = document.getElementById("config_input").value;
    let data = new Int32Array(dataText.split(",")).sort().join()

    let response = await fetch("/config", {
        method: 'POST',
        body: JSON.stringify({temperature: data.split(',')})
    });
    let result = response.ok;
    console.log(response.status);
    if (result) {
        document.getElementById("config_id").textContent = "(" + data + ")";
    } else {
        let eventElement = document.getElementById("event_id");
        let newElement = "<div class=\"errorState\"><a><i>" + new Date().toLocaleTimeString() + ":</i> error: " + response.status + " - " + await response.text();
        eventElement.insertAdjacentHTML('beforeend', newElement);
    }
}
