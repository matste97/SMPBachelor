// chartComponent.js

function createChart(containerId, channelData) {
    var data = JSON.parse(channelData).channelDemographic;
    var ctx = document.getElementById(containerId).getContext('2d');
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
            labels.push("No demographic data");
            maleData.push(0);
            femaleData.push(0);
        }


    // Create Chart
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Male',
                data: maleData,
                backgroundColor: 'rgba(54, 162, 235, 0.5)', // Blue color
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1
            },
                {
                    label: 'Female',
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
                        text: '% of viewers'
                    }
                },
                x: {
                    stacked: false,
                    title: {
                        display: true,
                        text: 'Demographic for channel'
                    }
                }
            }
        }
    });
}

