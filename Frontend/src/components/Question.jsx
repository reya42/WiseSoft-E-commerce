import React from 'react'

import { formatDate } from '../utils'

const Question = ({ data, singleProduct }) => {
  return (
    <div className={singleProduct ? "single_product_question" : "questions_question_container"}>
        <div className={singleProduct ? "single_product_question_user" : "questions_user"}>
            {`${data.user.userName[0]}${data.user.userName[1]}***** Tarafından`}
        </div>
        <div className={singleProduct ? "single_product_question_content" : "questions_content"}>
            {data.content}
        </div>
        <div className={singleProduct ? "single_product_question_date" : "questions_date"}>
            {formatDate(data.questionCreatedAt)}
        </div>
        {
            data.answer && (
                <div className={singleProduct ? "single_product_answer_container" : "questions_answer_container"}>
                    <div  className={singleProduct ? "single_product_answer" : "questions_answer"}>
                        {data.answer}
                    </div>
                    <div  className={singleProduct ? "single_product_answered_at" : "questions_answered_at"}>
                        {formatDate(data.questionAnsweredAt)}
                    </div>
                </div>
            )
        }
    </div>
  )
}

export default Question