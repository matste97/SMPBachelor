// Function to fetch data from the API and update the UI
function fetchData() {
    // Specify the API endpoint and access token
    const url = "https://graph.facebook.com/v12.0/241422063738/insights/page_impressions_by_age_gender_unique";
    const accessToken = "Placeholder";


    // Make the API call using fetch
    fetch(`${url}?access_token=${accessToken}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(responseData => {
            // Extract necessary data from the response
            const data = responseData.data;

            // Filter data for different time periods
            const dayData = data.find(entry => entry.period === 'day');
            const weekData = data.find(entry => entry.period === 'week');
            const days28Data = data.find(entry => entry.period === 'days_28');

            // Function to extract labels and values from filtered data
            const extractData = (entry) => {
                const valuesData = entry.values[0].value;
                const labels = Object.keys(valuesData);
                const values = Object.values(valuesData);

                // Define custom sorting order for labels
                const customOrder = ['F.13-17', 'M.13-17', 'U.13-17', 'F.18-24', 'M.18-24', 'U.18-24', 'F.25-34', 'M.25-34', 'U.25-34', 'F.35-44', 'M.35-44', 'U.35-44', 'F.45-54', 'M.45-54', 'U.45-54', 'F.55-64', 'M.55-64', 'U.55-64', 'F.65+', 'M.65+', 'U.65+'];

                // Create an array of objects with label and value pairs
                const dataPairs = labels.map((label, index) => ({ label, value: values[index] }));

                // Sort the array based on custom order of labels
                dataPairs.sort((a, b) => customOrder.indexOf(a.label) - customOrder.indexOf(b.label));

                // Extract sorted labels and values from the sorted array
                const sortedLabels = dataPairs.map(pair => pair.label);
                const sortedValues = dataPairs.map(pair => pair.value);

                return { labels: sortedLabels, values: sortedValues };
            };

            // Extract data for different time periods
            const dayChartData = extractData(dayData);
            const weekChartData = extractData(weekData);
            const days28ChartData = extractData(days28Data);

            // Color mapping for different data types
            const colorMap = {
                F: 'rgba(255, 0, 0, 0.2)', // Red for Female
                U: 'rgba(0, 255, 0, 0.2)', // Green for Unknown
                M: 'rgba(0, 0, 255, 0.2)', // Blue for Male
                
            };

            // Create separate charts with different colors
            createChart('dayChart', 'Page Impressions by Age and Gender (Day)', dayChartData.labels, dayChartData.values, colorMap);
            createChart('weekChart', 'Page Impressions by Age and Gender (Week)', weekChartData.labels, weekChartData.values, colorMap);
            createChart('days28Chart', 'Page Impressions by Age and Gender (28 Days)', days28ChartData.labels, days28ChartData.values, colorMap);
        })
        .catch(error => {
            console.error("Error fetching or processing data:", error);
        });
}

// Function to create a Chart.js instance with dynamic colors
function createChart(canvasId, title, labels, values, colorMap) {
  const ctx = document.getElementById(canvasId).getContext('2d');
  const backgroundColor = [];
  for (let i = 0; i < labels.length; i++) {
    const label = labels[i];
    const firstChar = label[0];
    backgroundColor.push(colorMap[firstChar]);
  }

  new Chart(ctx, {
    type: 'bar',
    data: {
      labels: labels,
      datasets: [{
        label: title,
        data: values,
        backgroundColor: backgroundColor,
        borderColor: 'rgba(0, 0, 0, 1)', // Use black for border
        borderWidth: 1
      }]
    },
    options: {
      scales: {
        y: {
          beginAtZero: true
        }
      }
    }
  });
}


// Call the fetchData function when the page loads
window.onload = fetchData;
