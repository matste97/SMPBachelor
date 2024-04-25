function createVideoCharts(jsonData) {
    var data = JSON.parse(jsonData);
    // Container to hold all charts
    var container = document.getElementById('VideosChartContainer');

    // Loop through each video object and create a chart
    for (var key in data) {
        if (key !== "DateTimeGathered") {

            var videoData = data[key];
            var individualChartContainer = document.createElement('div');
            individualChartContainer.className = 'video-chart-container';

            // Check if videoData exists and has the necessary properties
            if (videoData && videoData.videoTitle !== undefined) {
                // Chart Title and Views combined
                var titleAndViewsContainer = document.createElement('div');
                titleAndViewsContainer.className = 'title-views-container';
                individualChartContainer.appendChild(titleAndViewsContainer);

                // Video Title
                var thumbnail = document.createElement('img');
                var chartTitle = document.createElement('h2');
                var chartViews = document.createElement('p');
                var chartAvgView = document.createElement('p');
                chartTitle.className = 'chart-title';
                thumbnail.className = 'thumbnail';
                chartViews.className = 'chart-views';
                chartAvgView.className = 'chart-avgViews';
                chartTitle.textContent = videoData.videoTitle;
                thumbnail.setAttribute('src', videoData.thumbnail);
                chartViews.textContent = 'Totale visninger: ' + videoData.totalVideoViews;
                if (videoData.avgView && videoData.avgView.length > 0) {
                    var avgViewDuration = videoData.avgView[0].avgViewDurationinSec;
                    var averageViewPercentage = videoData.avgView[0].averageViewPercentage;
                    chartAvgView.textContent = 'Gjennomsnitt seertid: ' + avgViewDuration +" Sekunder (" +averageViewPercentage + "% av video)";

                } else {
                    chartViews.textContent = 'Gjennomsnitt ikke tilgjengelig';
                }
                titleAndViewsContainer.appendChild(chartTitle);
                titleAndViewsContainer.appendChild(thumbnail);
                titleAndViewsContainer.appendChild(chartViews);
                titleAndViewsContainer.appendChild(chartAvgView);

                var canvas = document.createElement('canvas');
                canvas.width = 400; // Set width to 400
                canvas.height = 400; // Set height to 400
                canvas.id = 'viewerChart_' + key;
                individualChartContainer.appendChild(canvas);
                container.appendChild(individualChartContainer);

                var datetimeInfo = document.createElement('div');
                datetimeInfo.innerHTML = "Data sist hentet: " + data.DateTimeGathered;
                individualChartContainer.appendChild(datetimeInfo);

                // Process the video demographic data into Chart.js compatible format
                var labels = [];
                var chartDataMale = [];
                var chartDataFemale = [];

                if (videoData.videoDemographic.length > 0) {
                    videoData.videoDemographic.forEach(function(item) {
                        var label = item.ageGroup;
                        if (!labels.includes(label)) {
                            labels.push(label);
                        }

                        if (item.gender === 'male') {
                            chartDataMale.push(item.viewerPercentage);
                        } else if (item.gender === 'female') {
                            chartDataFemale.push(item.viewerPercentage);
                        }
                    });
                } else {
                    // Provide default values if videoDemographic is empty
                    labels.push("Ikke noe data");
                    chartDataMale.push(0);
                    chartDataFemale.push(0);
                }

                // Create the chart
                var ctx = canvas.getContext('2d');
                var viewerChart = new Chart(ctx, {
                    type: 'bar',
                    data: {
                        labels: labels,
                        datasets: [{
                            label: 'Mann',
                            data: chartDataMale,
                            backgroundColor: 'rgba(54, 162, 235, 0.5)', // Blue color
                            borderColor: 'rgba(54, 162, 235, 1)',
                            borderWidth: 1
                        },
                            {
                                label: 'Kvinne',
                                data: chartDataFemale,
                                backgroundColor: 'rgba(255, 99, 132, 0.5)', // Red color
                                borderColor: 'rgba(255, 99, 132, 1)',
                                borderWidth: 1
                            }]
                    },
                    options: {
                        scales: {
                            y: {
                                beginAtZero: true,
                                stacked: false,
                                title: {
                                    display: true,
                                    text: '% av viewers'
                                }
                            },
                            x: {
                                stacked: false,
                                title: {
                                    display: true,
                                    text: 'Demografien for video'
                                }
                            }
                        }
                    }
                });
            } else {
                console.log('Error: Invalid video data for key:', key);
            }
        }
    }
}

// chartComponent.js

function createChannelChart(containerId, channelData) {
    var data = JSON.parse(channelData).channelDemographic;
    var container = document.getElementById(containerId);
    var ctx = container.getContext('2d');
    var labels = [];
    var maleData = [];
    var femaleData = [];


    // Prepare data for chart
    if (data.length > 0) {
        data.forEach(function (demographic) {
            var label = demographic.ageGroup;
            // Check if the label already exists in the labels array
            if (!labels.includes(label)) {
                labels.push(label);
            }

            if (demographic.gender === 'male') {
                maleData.push(demographic.viewerPercentage);
            } else if (demographic.gender === 'female') {
                femaleData.push(demographic.viewerPercentage);
            }
        });
    } else {
        labels.push("Ikke noe data");
        maleData.push(0);
        femaleData.push(0);
    }

    // Create Chart
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Mann',
                data: maleData,
                backgroundColor: 'rgba(54, 162, 235, 0.5)', // Blue color
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1
            },
                {
                    label: 'Kvinne',
                    data: femaleData,
                    backgroundColor: 'rgba(255, 99, 132, 0.5)', // Red color
                    borderColor: 'rgba(255, 99, 132, 1)',
                    borderWidth: 1
                }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true,
                    stacked: false,
                    title: {
                        display: true,
                        text: '% av viewers'
                    }
                },
                x: {
                    stacked: false,
                    title: {
                        display: true,
                        text: 'Demografien for kanalen'
                    }
                }
            }
        }
    });
    var datetimeInfo = document.createElement('div');
    datetimeInfo.innerHTML = "Data sist hentet: " + JSON.parse(channelData).DateTimeGathered;
    container.parentNode.appendChild(datetimeInfo);
}


function createVideoPieCharts(jsonData) {
    var data = JSON.parse(jsonData);
    var container = document.getElementById('VideosChartContainer');

    for (var key in data) {
        if (key !== "DateTimeGathered") {
            var videoData = data[key];
            var individualChartContainer = document.createElement('div');
            individualChartContainer.className = 'video-chart-container';

            if (videoData && videoData.videoTitle !== undefined) {
                var titleAndViewsContainer = document.createElement('div');
                titleAndViewsContainer.className = 'title-views-container';
                individualChartContainer.appendChild(titleAndViewsContainer);
                var thumbnail = document.createElement('img');
                var chartTitle = document.createElement('h2');
                var chartViews = document.createElement('p');
                var chartAvgView = document.createElement('p');
                chartTitle.className = 'chart-title';
                thumbnail.className = 'thumbnail';
                chartViews.className = 'chart-views';
                chartAvgView.className = 'chart-avgViews';
                chartTitle.textContent = videoData.videoTitle;
                thumbnail.setAttribute('src', videoData.thumbnail);
                chartViews.textContent = 'Totale visninger: ' + videoData.totalVideoViews;
                if (videoData.avgView && videoData.avgView.length > 0) {
                    var avgViewDuration = videoData.avgView[0].avgViewDurationinSec;
                    var averageViewPercentage = videoData.avgView[0].averageViewPercentage;
                    chartAvgView.textContent = 'Gjennomsnitt seertid: ' + avgViewDuration +" Sekunder (" +averageViewPercentage + "% av video)";

                } else {
                    chartViews.textContent = 'Gjennomsnitt ikke tilgjengelig';
                }
                titleAndViewsContainer.appendChild(chartTitle);
                titleAndViewsContainer.appendChild(thumbnail);
                titleAndViewsContainer.appendChild(chartViews);
                titleAndViewsContainer.appendChild(chartAvgView);


                var canvas = document.createElement('canvas');
                canvas.width = 400;
                canvas.height = 400;
                canvas.id = 'viewerChart_' + key;
                individualChartContainer.appendChild(canvas);
                container.appendChild(individualChartContainer);

                var datetimeInfo = document.createElement('div');
                datetimeInfo.innerHTML = "Data sist hentet: " + data.DateTimeGathered;
                individualChartContainer.appendChild(datetimeInfo);

                var labels = [];
                var chartData = [];
                var backgroundColors = [];
                var index = 1;

                if (videoData.videoDemographic.length > 0) {

                    videoData.videoDemographic.forEach(function(demographic) {
                        var label = demographic.gender + " " + demographic.ageGroup;
                        if (!labels.includes(label)) {
                            labels.push(label);
                        }
                        chartData.push(demographic.viewerPercentage);
                        backgroundColors.push(selectColor(index*3));
                        index++;
                    });
                } else {
                    labels.push("Ikke noe data");
                    chartData.push(0);
                }

                var ctx = canvas.getContext('2d');
                new Chart(ctx, {
                    type: 'pie',
                    data: {
                        labels: labels,
                        datasets: [{
                            data: chartData,
                            backgroundColor: backgroundColors,
                            borderColor: 'rgba(0, 0, 0, 1)',
                            borderWidth: 1}]
                    },
                });
            } else {
                console.log('Error: Invalid video data for key:', key);
            }
        }
    }
}

function createChannelPieChart(containerId, channelData) {
    var data = JSON.parse(channelData).channelDemographic;
    var container = document.getElementById(containerId);
    var ctx = container.getContext('2d');
    var labels = [];
    var chartData = [];
    var backgroundColors = [];
    var index = 1;

    if (data.length > 0) {
        data.forEach(function (demographic) {
            var label = demographic.gender + " " + demographic.ageGroup;
            if (!labels.includes(label)) {
                labels.push(label);
            }
            chartData.push(demographic.viewerPercentage);

            data.push(demographic.viewerPercentage);
            backgroundColors.push(selectColor(index*3));
            index++;
        });
    } else {
        labels.push("Ikke noe data");
        chartData.push(0);
    }

    new Chart(ctx, {
        type: 'pie',
        data: {
            labels: labels,
            datasets: [{
                data: chartData,
                backgroundColor: backgroundColors,
                borderColor: 'rgba(0, 0, 0, 1)',
                borderWidth: 1}]
        }
    });
    var datetimeInfo = document.createElement('div');
    datetimeInfo.innerHTML = "Data sist hentet: " + JSON.parse(channelData).DateTimeGathered;
    container.parentNode.appendChild(datetimeInfo);
}



function selectColor(number) {
    const hue = number * 137.508;
    return `hsl(${hue},50%,75%)`;
}

