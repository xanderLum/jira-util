#!/usr/bin/bash
#
# ./clone_jira_tickets.sh --token "xxxxx" --jql "project = 'SCRIPS' AND 'Test Category' = 'Smoke Test' AND 'Test Environment' = 'SIT'"
#

BASE_URL="<jira_host>:1443"

function usage()
{
  echo "USAGE: ./clone_jira_tickets.sh [-b|t|k|f|h]"
  echo "Options:"
  echo "-h|--help          : Print usage message"
  echo "-b|--base_url      : Jira BASE URL"
  echo "-t|--token         : Jira token for authendication"
  echo "-j|--jql           : Jira Query Language"
  exit 1
}

function error()
{
  echo "ERROR: ${1}" >&2
  exit 2
}

OPTS=$(getopt -a -o ha:b:t:k: --long help,app_name:,base_url:,token:,jql: -- "$@")

if [ $? != 0 ]; then
  error "Failed parsing otions."
fi

if [ "$#" -eq 0 ]; then
  usage
fi

eval set -- ${OPTS}

while :
do
  case "$1" in
    -h | --help )
      usage ;
      shift ;;
    -a | --app_name )
      APP_NAME="${2}" ;
      shift 2 ;;
    -b | --base_url )
      BASE_URL="${2}" ;
      shift 2 ;;
    -t | --token )
      JIRA_TOKEN="${2}" ;
      shift 2 ;;
    -k | --jql )
      JQL="${2}" ;
      shift 2 ;;
    -- )
      shift ;
      break ;;
    * )
      usage ;;
  esac
done
# Set initial values for pagination
startAt=0
maxResults=5

#Prepare post data request
generate_post_data()
{

cat <<EOF
{
  "jql": "${JQL}",
  "startAt": ${startAt},
  "maxResults": ${maxResults}
}
EOF

}

#Prepare post data for findIfExists in taget project : SCRTCXMAS
# Set initial values 
findJQL=""
findJQL_post_data()
{

cat <<EOF
{
  "jql": "${findJQL}",
  "startAt": 0,
  "maxResults": 1
}
EOF

}

# Loop to retrieve paginated results
while [ "${startAt}" -le 1 ] ; do

        #Log Request JSON
        echo "Page ${startAt/maxResults +1} REQUEST JSON: $(echo $(generate_post_data))"

        # Make the API call to search for issues based on the JQL query
        RESPONSE_WITH_HEADERS=$(curl -s -i -X POST \
                 -H "Authorization:Bearer ${JIRA_TOKEN}" \
                 -H "Content-Type: application/json" \
                 -H "Accept: application/json" \
                 -H "X-Atlassion-Token: no-check" \
                  ''$BASE_URL'/rest/api/latest/search' \
                  -d "$(generate_post_data)")

        #Check if RESPONSE is not empty
        echo "${RESPONSE_WITH_HEADERS}" > generated-json/full_response_page#${startAt/maxResults+1}.json

        JSON_RESPONSE=$(echo "${RESPONSE_WITH_HEADERS}" | awk '/^\r?$/{p=1;next}p' | tail -n +1)

        #Check if JSON_RESPONSE is not empty
        echo "${JSON_RESPONSE}" > generated-json/output_page#${startAt/maxResults+1}.json
        #more output_page#${startAt/maxResults+1}.json | jq -c ".issues[]"
        #more generated-json/output_page#${startAt/maxResults+1}.json | jq -c ".issues[].key"
        #more generated-json/output_page#${startAt/maxResults+1}.json | jq -c ".issues[].fields.summary"
        #more generated-json/output_page#${startAt/maxResults+1}.json | jq -c ".total"

        #Check if valid json
        if jq -e . >/dev/null 2>&1 <<< "${JSON_RESPONSE}"; then
                echo "Parsed JSON Successfully and got something other than false/null"
        else
                echo "Failed to parse JSON, or got false/null"
                echo "ERROR: Unable to retrieve Jira Tickets.  Check your JQL query and authendication."
                exit 1
        fi

        # Extract and print relevant information for each issue
        echo "Retrieved Jira tickets:"
        echo "-------------------------"

        # Process the retrieved isues
        echo "Retrieved issues on page ${startAt/maxResults +1}: "
		
		jq -c '.issues[]' <<< "$JSON_RESPONSE" | while IFS= read -r object; do
			echo "Processing object:"
			echo "$object" #Print the entire object
			
			#Access object fields using jq
			key=$(jq -r '.key' <<< "$object")
			summary=$(jq -r '.fields.summary' <<< "$object")
			testCaseId=$(jq -r '.fields.customfield_10415' <<< "$object")
			echo "Key: $key, Summary: $summary, TestCaseId: $testCaseId"
			
			#Prepare the JSON request to clone ticket
			request=$(jq 'del(.expand) | del(.id) | del(.self) | del(.key) | .fields.project="SCRTCXMAS"' <<< "$object")
			echo "${request}" > generated-json/request-${key}.json 
			#diff <(echo "$object") <(echo "$request")
			
			#Find if the existing request already exists in the target project
			#"project='SCRIPS' AND issueType='Test Case' AND status='No Run' AND 'QA Environment'=SIT"
			findJQL="project='SCRTCXMAS' AND issueType='Test Case' AND status='No Run' AND 'QA Environment'=SIT AND 'Test Case ID'~'${testCaseId}'"
			
			#Clone the ticket to SCRTCXMAS project
			SEARCH_RESPONSE_WITH_HEADERS=$(curl -s -i -X POST \
                 -H "Authorization:Bearer ${JIRA_TOKEN}" \
                 -H "Content-Type: application/json" \
                 -H "Accept: application/json" \
                 -H "X-Atlassion-Token: no-check" \
                  ''$BASE_URL'/rest/api/latest/search' \
                  -d "$(findJQL_post_data)")
				  
			#Check if RESPONSE is not empty
			echo "${SEARCH_RESPONSE_WITH_HEADERS}" > generated-json/findIfExist.json
			SEARCH_JSON_RESPONSE=$(echo "${SEARCH_RESPONSE_WITH_HEADERS}" | awk '/^\r?$/{p=1;next}p' | tail -n +1)
			echo "${SEARCH_JSON_RESPONSE}" > generated-json/searchjsonres.json
			
			# Check if the total returned tickets > 0
			searchTotal=$(jq '.total' <<< "$SEARCH_JSON_RESPONSE")
			declare searchTotal
			if [ "$searchTotal" -le 1 ]; then
				echo "The requested TestCase: $key to clone doesn't exists in Target Project.  Proceed to cloning ticket..."
				
				# Call API to clone ticket
				CREATE_RESPONSE_WITH_HEADERS=$(curl -s -i -X POST \
                 -H "Authorization:Bearer ${JIRA_TOKEN}" \
                 -H "Content-Type: application/json" \
                 -H "Accept: application/json" \
                 -H "X-Atlassion-Token: no-check" \
                  ''$BASE_URL'/rest/api/latest/issue' \
                  -d "$request")
				  
				#Check if RESPONSE is not empty
				echo "${CREATE_RESPONSE_WITH_HEADERS}" > generated-json/cloneticketres-${key}.json
				CREATE_JSON_RESPONSE=$(echo "${CREATE_RESPONSE_WITH_HEADERS}" | awk '/^\r?$/{p=1;next}p' | tail -n +1)
				echo "${CREATE_JSON_RESPONSE}" > generated-json/createjsonres-${key}.json
			else
				echo "The requested TestCase: $key to clone EXISTS already in Target Project.  Skipping this request..."
			fi
		done
		
        # Check if there are more pages
		total=$(jq '.total' <<< "$JSON_RESPONSE")
        declare -p startAt maxResults total
        startAt=$((startAt + maxResults))
        if [ "$startAt" -ge "$total" ]; then
                break
        fi
done
