from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from google.auth.transport.requests import Request
from googleapiclient.discovery import build
import json
import os

# Set up OAuth 2.0 credentials
SCOPES = ['https://www.googleapis.com/auth/youtube.readonly','https://www.googleapis.com/auth/yt-analytics.readonly']
CLIENT_SECRETS_FILE = 'secret.json'  # Place secret in same folder as script and declare it here

def get_authenticated_service(api_name, api_version, scopes):
    creds = None
    if os.path.exists('token.json'):
        creds = Credentials.from_authorized_user_file('token.json', scopes)
    # If there are no (valid) credentials available, let the user log in.
    if not creds or not creds.valid:
        if creds and creds.expired and creds.refresh_token:
            creds.refresh(Request())
        else:
            flow = InstalledAppFlow.from_client_secrets_file(
                CLIENT_SECRETS_FILE, scopes)
            creds = flow.run_local_server(port=8080)
        # Save the credentials for the next run
        with open('token.json', 'w') as token:
            token.write(creds.to_json())
    return build(api_name, api_version, credentials=creds)

def get_script_directory():
    return os.path.dirname(os.path.realpath(__file__))

def get_authenticated_analytics_service():
    return get_authenticated_service('youtubeAnalytics', 'v2', SCOPES)

def get_authenticated_data_service():
    return get_authenticated_service('youtube', 'v3', SCOPES)

# Get age and gender analytics for a specific video
def get_age_gender_analytics(youtube_analytics, video_id):
    analytics_response = youtube_analytics.reports().query(
        ids="channel==MINE",
        startDate="2013-01-01",
        endDate="2024-03-14",
        metrics="viewerPercentage",
        dimensions="ageGroup,gender",
        filters=f"video=={video_id}"
    ).execute()
    return analytics_response



# Get title of a specific video
def get_video_title(youtube_data, video_id):
    video_response = youtube_data.videos().list(
        part='snippet',
        id=video_id
    ).execute()
    return video_response['items'][0]['snippet']['title']

def get_video_ids(youtube_data):
    amount = 10 # You may need to paginate if there are more than 50 videos
    playlist_request = youtube_data.channels().list(
        part='contentDetails',
        mine=True,
    )

    playlist_response = playlist_request.execute()
    playlist_id = playlist_response['items'][0]['contentDetails']['relatedPlaylists']['uploads']
    videos_request = youtube_data.playlistItems().list(
        part='contentDetails',
        playlistId=playlist_id,
        maxResults=amount
    )
    print("Getting info for ", amount, " videos")
    videos_response = videos_request.execute()

    # Create a list to store video IDs
    video_ids = [item['contentDetails']['videoId'] for item in videos_response['items']]
    return (video_ids)

def get_channel_analytics(youtube_analytics):
    request = youtube_analytics.reports().query(
        ids='channel==mine',
        startDate='2015-01-01',
        endDate='2024-01-26',
        metrics='viewerPercentage',
        dimensions='ageGroup,gender'
    )
    response = request.execute()
    return response



# Save video demographic info to a JSON file
def save_channel_demographics_to_json(channel_analytics_data):
    channelData = {
        "ChannelDemographics": channel_analytics_data["rows"]
    }

    # Construct the file path for saving video information JSON
    script_dir = get_script_directory()
    channel_info_file_path = os.path.join(script_dir, 'channel_demographics.json')
        # Save simplified channel demographics data to JSON
    with open(channel_info_file_path, 'w') as json_file:
        json.dump(channelData, json_file, indent=4)


# Save video demographic info to a JSON file
def save_video_demographics_to_json(youtube_data, youtube_analytics):
    video_ids = get_video_ids(youtube_data)
    video_info = {}
    for video_id in video_ids:
        print("Getting info for: ", video_id)
        video_title = get_video_title(youtube_data, video_id)
        video_demographic_info = get_age_gender_analytics(youtube_analytics, video_id)
        simplified_video_info = {
            "title": video_title,
            "video demographics": video_demographic_info["rows"]
        }
        video_info[video_id] = simplified_video_info

    # Construct the file path for saving video information JSON
    script_dir = get_script_directory()
    video_info_file_path = os.path.join(script_dir, 'video_info.json')

    # Save video information to JSON
    with open(video_info_file_path, 'w') as json_file:
        json.dump(video_info , json_file, indent=4)


# Main function
def main():
    youtube_analytics = get_authenticated_analytics_service()
    youtube_data = get_authenticated_data_service()

    channel_analytics_data = get_channel_analytics(youtube_analytics)
    # Save channel analytics data to JSON
    save_channel_demographics_to_json(channel_analytics_data)
    save_video_demographics_to_json(youtube_data, youtube_analytics)

        
    print("Reached end")

if __name__ == '__main__':
    main()
