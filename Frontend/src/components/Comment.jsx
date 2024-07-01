import React from 'react'
import { formatDate } from '../utils'

const Comment = ({ data }) => {
  return (
    <div className="questions_question_container">
        <div className="questions_user">
            {`${data.user.userName[0]}${data.user.userName[1]}***** Tarafından`}
        </div>
        <div className="questions_content">
            {data.content}
        </div>
        <div className="questions_date">
            {formatDate(data.questionCreatedAt)}
        </div>
        {
            data.answer && (
                <div className="questions_answer_container">
                    <div  className="questions_answer">
                        {data.answer}
                    </div>
                    <div  className="questions_answered_at">
                        {formatDate(data.questionAnsweredAt)}
                    </div>
                </div>
            )
        }
    </div>
  )
}

export default Comment