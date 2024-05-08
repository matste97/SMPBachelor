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
  const groupedData = {};

  // Group data by gender
  Object.keys(valuesData).forEach(label => {
      const ageGroup = label.slice(2); // Extract age group (remove gender)
      const gender = label[0]; // Extract the first character (gender)
      if (!groupedData[gender]) {
          groupedData[gender] = [];
      }
      groupedData[gender].push({ label: ageGroup, value: valuesData[label] });
  });

  // Define custom order for age groups
  const customOrder = ['13-17', '18-24', '25-34', '35-44', '45-54', '55-64', '65+'];

  // Create an array of objects with label and value pairs
  const datasets = Object.keys(groupedData).map(gender => {
      const data = customOrder.map(ageGroup => {
          const found = groupedData[gender].find(item => item.label === ageGroup);
          return found ? found.value : 0;
      });
      return { label: gender, data: data };
  });

  // Extract sorted labels from custom order
  const sortedLabels = customOrder;

  return { labels: sortedLabels, datasets: datasets };
};


          // Extract data for different time periods
          const dayChartData = extractData(dayData);
          const weekChartData = extractData(weekData);
          const days28ChartData = extractData(days28Data);

          // Color mapping for different data types
          const colorMap = {
              F: 'rgba(255, 99, 132, 0.5)', // Red for Female
              U: 'rgba(57,161,53,0.5', // Green for Unknown
              M: 'rgba(54, 162, 235, 0.5)', // Blue for Male
              
          };

          // Create separate charts with different colors
          createChart('dayChart', 'Dag', dayChartData.labels, dayChartData.datasets, colorMap);
          createChart('weekChart', 'Uke', weekChartData.labels, weekChartData.datasets, colorMap);
          createChart('days28Chart', 'Måned', days28ChartData.labels, days28ChartData.datasets, colorMap);

          // Create toggle buttons
          createToggleButtons(dayChartData, 'dayChart');
          createToggleButtons(weekChartData, 'weekChart');
          createToggleButtons(days28ChartData, 'days28Chart');
      })
      .catch(error => {
          console.error("Error fetching or processing data:", error);
      });
}

// Function to create a Chart.js instance with dynamic colors
function createChart(canvasId, title, labels, datasets, colorMap) {
const ctx = document.getElementById(canvasId).getContext('2d');
const backgroundColor = [];
datasets.forEach(dataset => {
  const firstChar = dataset.label[0];
  backgroundColor.push(colorMap[firstChar]);
});

new Chart(ctx, {
  type: 'bar',
  data: {
    labels: labels,
    datasets: datasets.map(dataset => ({
      label: dataset.label,
      data: dataset.data,
      backgroundColor: colorMap[dataset.label[0]],
      borderColor: 'rgba(0, 0, 0, 1)', // Use black for border
      borderWidth: 1,
      hidden: dataset.hidden // Initially hide if specified
    }))
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

// Function to create toggle buttons for each gender
function createToggleButtons(chartData, chartId) {
const buttonContainer = document.createElement('div');
buttonContainer.classList.add('toggle-buttons');

chartData.datasets.forEach(dataset => {
  const button = document.createElement('button');
  button.textContent = dataset.label;
  button.classList.add('toggle-button');
  button.addEventListener('click', () => {
    const chart = Chart.getChart(chartId);
    const index = chart.data.datasets.findIndex(ds => ds.label === dataset.label);
    if (index !== -1) {
      chart.getDatasetMeta(index).hidden = !chart.getDatasetMeta(index).hidden;
      chart.update();
    }
  });
});

const canvasContainer = document.getElementById(chartId).parentNode;
canvasContainer.insertBefore(buttonContainer, canvasContainer.firstChild);
}

// Call the fetchData function when the page loads
window.onload = fetchData;
